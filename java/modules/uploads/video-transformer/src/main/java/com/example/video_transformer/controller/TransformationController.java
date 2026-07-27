package com.example.video_transformer.controller;

import com.example.uploads_api.transformations.dto.VideoTransformationTaskGroupDTO;
import com.example.video_transformer.task.VideoTransformationTaskMapper;
import com.example.video_transformer.task_service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transform")
@RequiredArgsConstructor
public class TransformationController {
    private final TaskService transformationService;

    @PostMapping()
    void process(@RequestBody VideoTransformationTaskGroupDTO body) {
        var tasks = VideoTransformationTaskMapper.createFromGroupedDTO(body);
        transformationService.processTasks(tasks);
    }
}
