define(['angular', './s3fileService', '../lib/playRoutes'], function (angular) {
    'use strict';
    var mod = angular.module('common.services.miz', ['common.lib.playRoutes', 'common.services.s3file']);
    mod.service('mizService', ['s3fileService', 'playRoutes', '$q', function (s3fileService, playRoutes, $q) {

        var self = this;
        var replaceInlineImgs = function (miz, mizTitle) {
            var deferred = $q.defer();
            if (!!miz.contentBody) {
                var filesToAdd = miz.contentBody.match(/<img src="data:image\/jpeg;base64,([^\s"]+)/g);
                if (!!filesToAdd && filesToAdd.length > 0) {
                    var body = {
                        'numIds': filesToAdd.length
                    };
                    playRoutes.api.controllers.MizController.requestS3Ids(mizTitle).post(body).then(function (response) {
                        var idsToUse = response.data;
                        var promises = [];
                        for (var ii in filesToAdd) {
                            var data = filesToAdd[ii].substring(filesToAdd[ii].indexOf('"')+1);
                            promises.push(s3fileService.updateS3(data, idsToUse[ii]));
                        }
                        $q.all(promises).then(function () {
                            for (var ii in filesToAdd) {
                                miz.contentBody = miz.contentBody.replace(filesToAdd[ii], '<img src="' + meekbiz.settings.s3host + idsToUse[ii].fileId + '.jpg');
                            }
                            deferred.resolve();
                        });
                    }, function () {
                        deferred.reject();
                    });
                } else {
                    deferred.resolve();
                }
            } else {
                deferred.resolve();
            }
            return deferred.promise;
        };

        self.createMiz = function (miz) {
            var deferred = $q.defer();
            playRoutes.api.controllers.MizController.createMiz().post(miz).then(function (response) {
                deferred.resolve(response.data);
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        self.getPrivateMiz = function (username, mizTitle) {
            var deferred = $q.defer();
            playRoutes.api.controllers.MizController.getPrivateMiz(username, mizTitle).get().then(function (response) {
                deferred.resolve(response.data)
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        self.checkIfMizExists = function (title) {
            return playRoutes.api.controllers.MizController.checkIfMizExists(title).get();
        };

        self.getPendingMizes = function () {
            var deferred = $q.defer();
            playRoutes.api.controllers.MizController.getPendingMizes().get().then(function (response) {
                deferred.resolve(response.data)
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        self.updateMiz = function (mizTitle, newMiz) {
            var deferred = $q.defer();
            replaceInlineImgs(newMiz, mizTitle).then(function () {
                playRoutes.api.controllers.MizController.updateMiz(mizTitle).post(newMiz).then(function (response) {
                    deferred.resolve(response.data)
                }, function () {
                    deferred.reject();
                });
            }, function () {
                deferred.reject();
            });

            return deferred.promise;
        };

        self.deleteMiz = function (username, mizTitle) {
            var deferred = $q.defer();
            playRoutes.api.controllers.MizController.deleteMizThatHasNeverBeenActivated(username, mizTitle)['delete'].call().then(function () {
                deferred.resolve();
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };
    }]);
    return mod;
});