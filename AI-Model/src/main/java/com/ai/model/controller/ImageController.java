package com.ai.model.controller;

import com.ai.model.config.model.AiModelFactory;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image")
public class ImageController {


    private final OpenAiImageModel aiModel;

    public ImageController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory) {
        this.aiModel = (OpenAiImageModel) aiModelFactory.getModel("openai-image");
    }

    /**
     * 同步接口响应对话
     * @param input 用户输入
     * @return 回复内容
     */
    @GetMapping("/singleImage")
    public String chat(@RequestParam("input") String input) {
        ImageOptions options = ImageOptionsBuilder.builder()
                .model("wanx2.1-t2i-turbo")
                .height(1024)
                .width(1024)
                .build();
        ImagePrompt prompt = new ImagePrompt(input, options);
        ImageResponse call = aiModel.call(prompt);
        return call.getResult().getOutput().getUrl();
    }


}
