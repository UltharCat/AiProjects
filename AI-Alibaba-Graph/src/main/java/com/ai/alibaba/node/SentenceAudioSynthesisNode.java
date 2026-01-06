package com.ai.alibaba.node;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public class SentenceAudioSynthesisNode implements NodeAction {

    private final TextToSpeechModel speechSynthesisModel;

    public SentenceAudioSynthesisNode(TextToSpeechModel speechSynthesisModel) {
        this.speechSynthesisModel = speechSynthesisModel;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("SentenceAudioSynthesisNode State {}", state);
        // 获取翻译后句子
        String translate = state.value("translate", "");
        // 调用语音合成模型进行合成
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(translate,
                DashScopeAudioSpeechOptions.builder().model(DashScopeModel.AudioModel.COSYVOICE_V1.getValue()).build());
        Path filePath = Path.of("AI-Alibaba-Graph/src/main/resources/gen/tts");
        Files.createDirectories(filePath);
        String fileName = "output.mp3";
        Path output = filePath.resolve(fileName);

        try (FileOutputStream fos = new FileOutputStream(output.toFile())) {
            byte[] audioOutput = speechSynthesisModel.call(prompt).getResult().getOutput();
            fos.write(audioOutput);
        }
        return Map.of("audio", output.toAbsolutePath().toString());
    }
}
