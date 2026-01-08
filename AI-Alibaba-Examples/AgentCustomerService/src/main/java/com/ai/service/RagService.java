package com.ai.service;

import java.io.File;

public interface RagService {

    boolean insertContent(String content);

    boolean uploadFile(File file);

}
