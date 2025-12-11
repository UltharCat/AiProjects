package com.ai.alibaba.controller;

import com.ai.alibaba.config.model.AiModelFactory;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/audio/transcription")
public class AudioTranscriptionController {

    private final AudioTranscriptionModel aiModel;

    public AudioTranscriptionController(@Qualifier("aiModelFactory") AiModelFactory aiModelFactory) {
        this.aiModel = (AudioTranscriptionModel) aiModelFactory.getModel("dashscope-audio-transcription");
    }

    /**
     * 同步接口响应对话
     * @return 回复内容
     */
    @GetMapping("/singleTranscription")
    public String transcription() throws MalformedURLException {
        Resource resource = new UrlResource("https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female.wav");
        DashScopeAudioTranscriptionOptions options = DashScopeAudioTranscriptionOptions.builder()
                .model("sensevoice-v1")
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
        return aiModel.call(prompt).getResult().getOutput();
    }


}
