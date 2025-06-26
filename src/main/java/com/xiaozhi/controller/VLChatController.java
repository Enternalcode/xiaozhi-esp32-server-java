package com.xiaozhi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaozhi.common.interceptor.UnLogin;
import com.xiaozhi.dialogue.llm.factory.ChatModelFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 视觉对话
 */
@RestController
public class VLChatController extends BaseController {

    @Resource
    private ChatModelFactory chatModelFactory;

    /**
     * 视觉对话
     */
    @UnLogin
    @PostMapping(value = "/vl/chat", produces = "application/json;charset=UTF-8")
    public String vlChat(@RequestParam("file") MultipartFile file,
                         @RequestParam String question,
                         HttpServletRequest request) {
        try {
            ChatModel chatModel = chatModelFactory.takeVisionModel();
            MimeType mimeType = MimeType.valueOf(file.getContentType());
            Media media = Media.builder()
                    .mimeType(mimeType)
                    .data(file.getResource())
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .media(media)
                    .text(question)
                    .build();
            String call = chatModel.call(userMessage);
            logger.info("问题：{}，图文识别内容：{}", question, call);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("text", call);
            String string = new ObjectMapper().writeValueAsString(result);
            logger.info("json结果:{}", string);

            return string;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "无可以使用的视觉模型";
        }
    }
}