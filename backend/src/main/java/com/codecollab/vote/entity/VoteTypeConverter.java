package com.codecollab.vote.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VoteTypeConverter implements AttributeConverter<VoteType, Short> {
    
    @Override
    public Short convertToDatabaseColumn(VoteType v) {
        return v == null ? null : (short) v.value();
    }
    
    @Override
    public VoteType convertToEntityAttribute(Short v) {
        return v == null ? null : VoteType.fromValue(v);
    }
}
