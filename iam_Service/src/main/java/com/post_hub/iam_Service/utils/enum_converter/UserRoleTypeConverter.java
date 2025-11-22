package com.post_hub.iam_Service.utils.enum_converter;

import com.post_hub.iam_Service.service.model.IamServiceUserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleTypeConverter implements AttributeConverter<IamServiceUserRole, String> {
    @Override
    public String convertToDatabaseColumn(IamServiceUserRole iamServiceUserRole) {

        return iamServiceUserRole.name();
    }
    @Override
    public IamServiceUserRole convertToEntityAttribute(String s) {
        return IamServiceUserRole.fromName(s);
    }

}
