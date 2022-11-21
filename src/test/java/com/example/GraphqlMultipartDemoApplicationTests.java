package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;


@SpringBootTest
class GraphqlMultipartDemoApplicationTests {

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

