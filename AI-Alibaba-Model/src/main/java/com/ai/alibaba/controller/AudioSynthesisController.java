package com.ai.alibaba.controller;

import com.ai.alibaba.config.model.AiModelFactory;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeSpeechSynthesisOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.ByteBuffer;

@RestController
@RequestMapping("/audio/synthesis")
public class AudioSynthesisController {

    private final DashScopeSpeechSynthesisModel aiModel;

    public AudioSynthesisController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory) {
        this.aiModel = (DashScopeSpeechSynthesisModel) aiModelFactory.getModel("dashscope-audio-synthesis");
    }

    /**
     * 同步接口响应对话
     * @param input 用户输入
     * @return 回复内容
     */
    @GetMapping("/singleSynthesis")
    public ResponseEntity<byte[]> chat(@RequestParam("input") String input) {
        SpeechSynthesisPrompt prompt = new SpeechSynthesisPrompt(input);
        ByteBuffer audio = aiModel.call(prompt).getResult().getOutput().getAudio();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"synthesis.mp3\"")
                .body(audio.array());
    }


}
