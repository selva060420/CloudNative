# Cloud related information

This repository contains information related to cloud native applications and tech used.

## AWS-CLI

- Below are the steps to install aws cli:

```
sudo su
cd /
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

### AWS Configuration:

- Check the .aws/ directory if present under $HOME. It contains config and credential file. 

- Below are the steps to configure if not present:

```
aws configure (with and without --profile) 
or
aws configure import --csv file://<path of csv file>
aws configure wizard ----> help

complete -C aws_completer aws  --> for auto tab completion
cli_auto_prompt = on-partial ---> append this in .aws/config file for interactive mode.
cli_pager =   ---> append this in .aws/config file for not opening vi editor with json/yaml output format.
```

### Run Instance:

```
aws ec2 run-instances \
    --image-id ami-07ffb2f4d65357b42 \
    --count 1 \
    --instance-type t2.micro \
    --key-name kumar \
    --security-group-ids <group-id> \
    --subnet-id <subnet-id> \
    --block-device-mappings "[{\"DeviceName\":\"/dev/sda1\",\"Ebs\":{\"VolumeSize\":30,\"DeleteOnTermination\":true}}]" \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=test}]' 'ResourceType=volume,Tags=[{Key=Name,Value=test-disk}]'
```

#### Launch instance using template:

- First create a template with ubuntu image, 1 CPU, security group and 30 GB disk in AWS Web console and get the template id.

```
aws ec2 run-instances \
    --launch-template LaunchTemplateId=<template-id> \
	--subnet-id  <subnet-id>\
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=test}]' 'ResourceType=volume,Tags=[{Key=Name,Value=test-disk}]'
```
