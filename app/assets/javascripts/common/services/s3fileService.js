define(['angular', '../lib/playRoutes'], function (angular) {
    'use strict';
    var mod = angular.module('common.services.s3file', ['common.lib.playRoutes']);
    mod.service('s3fileService', ['playRoutes', '$q', '$http',
        function (playRoutes, $q, $http) {

        var self = this;

        var dataURLToBlob =  function(dataURL) {
            var BASE64_MARKER = ';base64,';
            if (dataURL.indexOf(BASE64_MARKER) == -1) {
                var parts = dataURL.split(',');
                var contentType = parts[0].split(':')[1];
                var raw = decodeURIComponent(parts[1]);

                return new Blob([raw], {type: contentType});
            }

            var parts = dataURL.split(BASE64_MARKER);
            var contentType = parts[0].split(':')[1];
            var raw = window.atob(parts[1]);
            var rawLength = raw.length;

            var uInt8Array = new Uint8Array(rawLength);

            for (var i = 0; i < rawLength; ++i) {
                uInt8Array[i] = raw.charCodeAt(i);
            }

            return new Blob([uInt8Array], {type: contentType});
        };

        self.updateS3 = function (fileToSend, backendMetaData, deleteOnfailure, deferred) {
            if (!deferred) {
                deferred = $q.defer();
            }
            deleteOnfailure = !!deleteOnfailure;

            var fd = new FormData();
            fd.append('key', backendMetaData.fileId + '.jpg');
            fd.append('AWSAccessKeyId', backendMetaData.accessKey);
            fd.append('acl', 'public-read');
            fd.append('success_action_status', '201');
            fd.append('policy', backendMetaData.policy );
            fd.append('signature', backendMetaData.signature);
            fd.append('Content-Type', 'image/jpeg');
            fd.append('file', dataURLToBlob(fileToSend));

            $http.post(meekbiz.settings.s3host , fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            }).success(function () {
                deferred.resolve(backendMetaData.fileId);
            }).error(function () {
                console.log('upload to s3 failed');
                if (deleteOnfailure) {
                    playRoutes.api.controllers.S3Controller.deleteS3FileMetaData(backendMetaData.fileId)['delete'].call();
                }
                deferred.reject();
            });
            return deferred.promise;
        }

        self.createFile = function (file) {
            var deferred = $q.defer();
            playRoutes.api.controllers.S3Controller.createS3FileMetaData().post().then(function(response) {
                self.updateS3(file, response.data, true, deferred);
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        self.updateFile = function (s3fileId, file) {
            var deferred = $q.defer();
            playRoutes.api.controllers.S3Controller.updateS3FileMetaData(s3fileId).put().then(function(response) {
                self.updateS3(file, response.data, false, deferred);
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        }

    }]);
    return mod;
});