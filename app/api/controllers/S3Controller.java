package api.controllers;

import api.entity.S3File;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.Authenticated;

public class S3Controller extends Controller {

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result createS3FileMetaData() {
        S3File s3File = new S3File();
        s3File.save(request().username());
        return ok(s3File.createPolicyResponseNode());
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result deleteS3FileMetaData(String publicId) {
        S3File.delete(publicId, request().username());
        return ok();
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result updateS3FileMetaData(String publicId) {
        S3File s3File = S3File.getByPublicId(publicId);
        if (s3File.getOwnerId().equals(request().username())) {
            return ok(s3File.createPolicyResponseNode());  //has permission to update the s3File
        } else {
            return forbidden(); //user does not have permission to update the file
        }
    }
}