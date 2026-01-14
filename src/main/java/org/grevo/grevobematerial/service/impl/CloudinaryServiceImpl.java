package org.grevo.grevobematerial.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.grevo.grevobematerial.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        Map<String, Object> uploadParams = new HashMap<>();
        uploadParams.put("folder", folder);
        uploadParams.put("resource_type", "auto");

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            return;
        }

        // Delete from Cloudinary
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
