import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;

public class Main {
    private static final String MANAGER_TAG = "Manager";

    public static void main(String[] args) {
        Ec2Client ec2 = Ec2Client.create();
        String managerAMI = args[0];
        String awsAccessKeyId = args[1];
        String awsSecretAccessKey = args[2];
        String script = String.join("\n",
                "set -e -x",
                String.format("aws configure set aws_access_key_id %s", awsAccessKeyId),
                String.format("aws configure set aws_secret_access_key", awsSecretAccessKey),
                "cd ..",
                "cd /home/ec2-user",
                "if [ -d \"DistPDFParserManager\" ]; then git clone https://github.com/rotba/DistPDFParserManager.git; fi",
                "cd DistPDFParserManager",
                "git pull",
                "mvn install",
                "cd target",
                "java -jar theJar.jar"
        );
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(managerAMI)
                .instanceType(InstanceType.T2_MICRO)
                .maxCount(1)
                .minCount(1)
                .keyName("myKeyPair")
                .userData(Base64.getEncoder().encodeToString(script.getBytes()))
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();
        Tag tag = Tag.builder()
                .key("Name")
                .value(MANAGER_TAG)
                .build();

        CreateTagsRequest tagsRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagsRequest);
            System.out.printf(
                    "Successfully started EC2 instance %s based on AMI %s",
                    instanceId, managerAMI);
        }catch (Ec2Exception e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done");
    }
}
