package com.example;

import com.example.config.GraphQlConfiguration;
import com.example.controller.FileUploadController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;


@SpringBootTest
@AutoConfigureHttpGraphQlTester
class GraphqlMultipartDemoApplicationTests {

	@Autowired
	private HttpGraphQlTester graphQlTester;

	@Test
	void contextLoads() throws Exception {
		MultipartFile file = getMultipartFile("/Users/eopoku/Downloads/istockphoto-1298942276-612x612.jpg");
		Assertions.assertTrue(file.getResource().exists());


	}

	private MockMultipartFile getMultipartFile(String fileName) {
		return new MockMultipartFile(
				fileName,
				String.format("%s.png", fileName),
				"image",
				fileName.getBytes(StandardCharsets.UTF_8));
	}

}

