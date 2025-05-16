package com.thesss.platform.workflows.model.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChangeData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String previousValue;
    private String newValue;
}