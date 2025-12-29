package io.github.oldmerman.web.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Value("${alias.oss.access-key}")
    private String accessKey;

    @Value("${alias.oss.secret-key}")
    private String secretKey;

    @Value("${alias.oss.endpoint}")
    private String endpoint;

    @Value("${alias.oss.region}")
    private String region;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() throws ClientException {
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .clientConfiguration(clientBuilderConfiguration)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

}
