package com.example.springai.controller;

import com.example.springai.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
class ChatController {
	private final ChatClient chatClient;
	private final OpenAiAudioTranscriptionModel transcriptionModel;
	private final Environment environment;
	@Value("${custom.summary.prompt}")
	private String summaryPrompt;
	@Value("${custom.summary.file-path}")
	private String summaryFilePath;
	@Value("${custom.srt.file-path}")
	private String srtFilePath;
	@Value("${custom.audio.file-path}")
	private String audioFilePath;

	@GetMapping("/joke")
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke in Korean") String message) {
		return Map.of(
				"completion",
				chatClient.prompt()
						.user(message)
						.call()
						.content());
	}

	@GetMapping("/chat")
	public ChatResponse chat(@RequestParam(value = "message", defaultValue = "Response in Korean") String message) {
		ChatResponse chatResponse = chatClient.prompt()
				.user(message)
				.call()
				.chatResponse();

		return chatResponse;
		/*
		{
		  "result": {
			"output": {
			  "messageType": "ASSISTANT",
			  "metadata": {
				"refusal": "",
				"finishReason": "STOP",
				"index": 0,
				"id": "chatcmpl-AFX4N1IZS1jnTlSMt8wviRH3t9jZx",
				"role": "ASSISTANT",
				"messageType": "ASSISTANT"
			  },
			  "toolCalls": [

			  ],
			  "content": "저는 AI 언어 모델이기 때문에 특정한 이름은 없지만, 여러분이 저를 어떻게 부르든지 괜찮습니다. 도움이 필요하시면 언제든지 말씀해 주세요!"
			},
			"metadata": {
			  "finishReason": "STOP",
			  "contentFilterMetadata": null
			}
		  },
		  "metadata": {
			"id": "chatcmpl-AFX4N1IZS1jnTlSMt8wviRH3t9jZx",
			"model": "gpt-4o-mini-2024-07-18",
			"rateLimit": {
			  "requestsLimit": 10000,
			  "requestsRemaining": 9999,
			  "tokensLimit": 200000,
			  "tokensRemaining": 199976,
			  "requestsReset": "PT1M4S",
			  "tokensReset": "PT0.007S"
			},
			"usage": {
			  "totalTokens": 58,
			  "promptTokens": 17,
			  "generationTokens": 41
			},
			"promptMetadata": [

			],
			"empty": false
		  },
		  "results": [
			{
			  "output": {
				"messageType": "ASSISTANT",
				"metadata": {
				  "refusal": "",
				  "finishReason": "STOP",
				  "index": 0,
				  "id": "chatcmpl-AFX4N1IZS1jnTlSMt8wviRH3t9jZx",
				  "role": "ASSISTANT",
				  "messageType": "ASSISTANT"
				},
				"toolCalls": [

				],
				"content": "저는 AI 언어 모델이기 때문에 특정한 이름은 없지만, 여러분이 저를 어떻게 부르든지 괜찮습니다. 도움이 필요하시면 언제든지 말씀해 주세요!"
			  },
			  "metadata": {
				"finishReason": "STOP",
				"contentFilterMetadata": null
			  }
			}
		  ]
		}
		 */
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8")
	public Flux<String> stream(@RequestParam(value = "message", defaultValue = "Response in Korean") String message) {

		Flux<String> content = chatClient.prompt()
				.user(message)
				.stream()
				.content();

		return content;
	}

	@GetMapping( "/transcription")
	public String youtube(@RequestParam String fileName) {
		String path = String.join("/",
				environment.getProperty("user.dir"),
				audioFilePath,
				fileName
		);
		Resource audioResource = new FileSystemResource(path);
		String text = transcriptionModel.call(audioResource);

		String originalFileName = fileName.split("\\.")[0];
		FileUtil.save(text, srtFilePath + "/" + originalFileName + ".srt");

		return text;
	}

	@GetMapping(value = "/summary", produces = MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8")
	public Flux<String> summary(@RequestParam(value = "fileName") String fileName) {

		String contents = FileUtil.read(srtFilePath + "/" + fileName);
		String prompt = String.format(summaryPrompt, contents);

		Flux<String> response = chatClient.prompt()
				.user(prompt)
				.stream()
				.content();

		String mdFile = fileName.split("\\.")[0] + ".md";

		StringBuilder summaryText = new StringBuilder();

		return response
				.doOnNext(summaryText::append)
				.doFinally(text -> {
					FileUtil.save(summaryText.toString(), summaryFilePath + "/" + mdFile);
				}
		);
	}
}