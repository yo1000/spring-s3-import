package com.yo1000.s3import.application;

import com.yo1000.s3import.config.TimeProperties;
import com.yo1000.s3import.domain.Node;
import com.yo1000.s3import.domain.NodeRepository;
import com.yo1000.s3import.domain.vo.NodeIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Transactional
@Service
public class NodeApplicationService {
    private final NodeRepository nodeRepository;
    private final TimeProperties timeProps;
    private final NodeIdHolder nodeIdHolder;

    private final Logger logger = LoggerFactory.getLogger(NodeApplicationService.class);

    public NodeApplicationService(NodeRepository nodeRepository, TimeProperties timeProps, NodeIdHolder nodeIdHolder) {
        this.nodeRepository = nodeRepository;
        this.timeProps = timeProps;
        this.nodeIdHolder = nodeIdHolder;
    }

    public boolean checkInit(long execTime) {
        if (nodeRepository.findById(nodeIdHolder.value()).isEmpty()) {
            if (nodeRepository.findById(Node.INIT_CHECK.id()).isPresent()) {
                sleep(execTime, timeProps.getWaitUnitTimeMillis());
            } else {
                try {
                    nodeRepository.save(Node.INIT_CHECK);
                } catch (DuplicateKeyException e) {
                    logger.warn(e.getMessage());
                    sleep(execTime, timeProps.getWaitUnitTimeMillis());
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void await(long execTime) {
        nodeRepository.findById(nodeIdHolder.value())
                .filter(node -> node.rank() >= 0)
                .ifPresent(node -> {
                    long sleepMillis = node.rank() * timeProps.getWaitUnitTimeMillis();
                    sleep(execTime, sleepMillis);
                });
    }

    public void register(long execTime) {
        nodeRepository.save(new Node(nodeIdHolder.value(), -1, execTime));
    }

    public void updateRank(long execTime) {
        Iterable<Node> allNodes = nodeRepository.findAll();
        List<Node> sortedNodes = StreamSupport.stream(allNodes.spliterator(), false)
                .filter(n -> !Objects.equals(n.id(), Node.INIT_CHECK.id()))
                .sorted((o1, o2) -> Comparator.<String>naturalOrder().compare(o1.id(), o2.id()))
                .toList();

        for (int i = 0; i < sortedNodes.size(); i++) {
            Node currNode = sortedNodes.get(i);
            Node saveNode = Objects.equals(currNode.id(), nodeIdHolder.value())
                    ? currNode.update(i, execTime)
                    : currNode.update(i);

            nodeRepository.save(saveNode);
        }
    }

    public void cleanup(long execTime) {
        long expiredTime = execTime - timeProps.getNodeKeepAliveMillis();
        nodeRepository.deleteByLastModifiedEpochMillisBefore(expiredTime);
    }

    private void sleep(long execTime, long sleepMillis) {
        try {
            logger.info("Node={} Time={} | Sleep {}-millis.", nodeIdHolder.value(), execTime, sleepMillis);
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        }
    }
}
