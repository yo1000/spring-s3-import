#!/bin/bash

awslocal s3api create-bucket --bucket $BUCKET_NAME

awslocal s3api put-object \
  --bucket $BUCKET_NAME \
  --key $DEST_OBJECT_PATH \
  --body $SRC_FILE_PATH
