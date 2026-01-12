package org.grevo.grevobematerial.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {

    /**
     * Upload image to Cloudinary
     * 
     * @param file   MultipartFile to upload
     * @param folder Folder name in Cloudinary (e.g., "grevo/avatars")
     * @return Secure URL of uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(MultipartFile file, String folder) throws IOException;

    /**
     * Delete image from Cloudinary
     * 
     * @param publicId Public ID of the image to delete
     * @throws IOException if deletion fails
     */
    void deleteImage(String publicId) throws IOException;
}
