package api.entity;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.PlayConfigUtil;

import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PlayConfigUtil.class)
@PowerMockIgnore("javax.crypto.*")
public class S3FileSpec {
    String fileId;
    Date expiry;

    @Before
    public void setup() {
        fileId = "anFileId";
        expiry = new Date(1413859702578L);
        PowerMockito.mockStatic(PlayConfigUtil.class);
        when(PlayConfigUtil.getConfig(eq("aws.s3.bucket"))).thenReturn("img-dev.meekbiz.com");
        when(PlayConfigUtil.getConfig(eq("aws.secret.key"))).thenReturn("MRRm+cHomV8jYSs5CDOAIKAQR9AqvvLq4IrOOZY9");
        when(PlayConfigUtil.getConfig(eq("aws.access.key"))).thenReturn("AKIAJVTDJH6AE7FSEJTQ");
    }

    @Test
    public void getPolicyTest() {
        String expected = "eydleHBpcmF0aW9uJzogJzIwMTQtMTAtMjFUMDI6NDg6MjJaJywnY29uZGl0aW9ucyc6IFt7J2J1Y2tldCc6ICdpbWctZGV2Lm1lZWtiaXouY29tJ30sWydlcScsICcka2V5JywgJ2FuRmlsZUlkLmpwZyddLHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30seydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sWydzdGFydHMtd2l0aCcsICckQ29udGVudC1UeXBlJywgJyddLFsnY29udGVudC1sZW5ndGgtcmFuZ2UnLCAwLCAxMDQ4NTc2XV19";
        String actual = S3File.getPolicy(fileId, expiry);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
     public void getSignatureTest() {
        String policy = "eydleHBpcmF0aW9uJzogJzIwMTQtMTAtMjFUMDI6NDg6MjJaJywnY29uZGl0aW9ucyc6IFt7J2J1Y2tldCc6ICdpbWctZGV2Lm1lZWtiaXouY29tJ30sWydlcScsICcka2V5JywgJ2FuRmlsZUlkLmpwZyddLHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30seydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sWydzdGFydHMtd2l0aCcsICckQ29udGVudC1UeXBlJywgJyddLFsnY29udGVudC1sZW5ndGgtcmFuZ2UnLCAwLCAxMDQ4NTc2XV19";
        String expected = "Aql6ntKJWgy3XjyKpNUG3XZ88ls=";
        String actual = S3File.getSignature(policy);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getPolicyResponseNode() {
        JsonNode node = S3File.createPolicyResponseNode(fileId, expiry);
        assertThat(node.get("fileId").asText()).isEqualTo(fileId);
        assertThat(node.get("policy").asText()).isEqualTo("eydleHBpcmF0aW9uJzogJzIwMTQtMTAtMjFUMDI6NDg6MjJaJywnY29uZGl0aW9ucyc6IFt7J2J1Y2tldCc6ICdpbWctZGV2Lm1lZWtiaXouY29tJ30sWydlcScsICcka2V5JywgJ2FuRmlsZUlkLmpwZyddLHsnYWNsJzogJ3B1YmxpYy1yZWFkJ30seydzdWNjZXNzX2FjdGlvbl9zdGF0dXMnOiAnMjAxJ30sWydzdGFydHMtd2l0aCcsICckQ29udGVudC1UeXBlJywgJyddLFsnY29udGVudC1sZW5ndGgtcmFuZ2UnLCAwLCAxMDQ4NTc2XV19");
        assertThat(node.get("accessKey").asText()).isEqualTo("AKIAJVTDJH6AE7FSEJTQ");
        assertThat(node.get("signature").asText()).isEqualTo("Aql6ntKJWgy3XjyKpNUG3XZ88ls=");
        assertThat(node.get("bucket").asText()).isEqualTo("img-dev.meekbiz.com");
    }


}
