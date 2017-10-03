define(['angular-mock', 'common'], function () {
    describe("s3fileService", function () {
        var mockPlayRoutes, s3fileService, scope, q, deferred, promiseResponse, promiseError, amazonPromiseError, amazonPromiseResolve;
        beforeEach(function () {
            module('common.services.s3file', function ($provide) {
                var mockHttp = {
                    post: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    put: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    delete: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(mockHttp, 'post').andCallThrough();
                spyOn(mockHttp, 'put').andCallThrough();
                spyOn(mockHttp, 'delete').andCallThrough();

                mockPlayRoutes = {
                    api: {
                        controllers: {
                            S3Controller: {
                                createS3FileMetaData: function () {
                                    return mockHttp;
                                },
                                updateS3FileMetaData: function (fileId) {
                                    return mockHttp;
                                },
                                deleteS3FileMetaData: function (fileId) {
                                    return mockHttp;
                                }
                            }
                        }
                    }
                };
                spyOn(mockPlayRoutes.api.controllers.S3Controller, 'updateS3FileMetaData').andCallThrough();
                spyOn(mockPlayRoutes.api.controllers.S3Controller, 'deleteS3FileMetaData').andCallThrough();

                var mockCallToAmazon = {
                    post: function (a, b, c) {
                        var successCb, errorCb;
                        amazonPromiseResolve = function () {
                            if (amazonPromiseError) {
                                errorCb();
                            } else {
                                successCb();
                            }
                        };
                        return {
                            success: function (successCallback) {
                                successCb = successCallback;
                                return this;
                            },
                            error: function (errorCallback) {
                                errorCb = errorCallback;
                                return this;
                            }
                        }
                    }
                }
                $provide.value('playRoutes', mockPlayRoutes);
                $provide.value('$http', mockCallToAmazon);
            });
        });
        beforeEach(function () {
            inject(function (_s3fileService_, $rootScope, $q) {
                scope =  $rootScope;
                q = $q;
                s3fileService = _s3fileService_;
            });
            promiseError = false;
            amazonPromiseError = false;
            promiseResponse = {};
            window.meekbiz = {};
            window.meekbiz.settings = {};
            window.meekbiz.settings.s3host = "http://s3.amazon.com";
        });

        describe('when creating a new s3file', function () {
            var fileIdResult = null;
            beforeEach(function () {
                fileIdResult = null;
            });
            describe('when permission is denied', function () {
                var calledFailure = false;
                beforeEach(function () {
                    promiseError = true;
                    s3fileService.createFile('akdjf9030f3==').then(function (fileId) {
                        fileIdResult = fileId;
                    },
                    function () {
                        calledFailure = true;
                    });
                    scope.$digest();
                });
                it("should call backend", function () {
                    expect(mockPlayRoutes.api.controllers.S3Controller.createS3FileMetaData().post).toHaveBeenCalled();
                });
                it("should have call failure", function () {
                    expect(calledFailure).toBeTruthy();
                });
            });

            describe('when permission is successful', function () {
                beforeEach(function () {
                    promiseError = false;
                });
                describe('when call to s3 is successful', function () {
                    beforeEach(function () {
                        amazonPromiseError = false;
                        promiseResponse = {
                            data: {
                                fileId: 'aFileId',
                                accessKey: 'AJFLWG**E9)',
                                policy: 'lkjdsflkjjelejedsfefe=',
                                signature: '9lkfdjlf983=='
                            }
                        };
                        s3fileService.createFile('akdjf9030f3==').then(function (fileId) {
                            fileIdResult = fileId;
                        });
                        scope.$digest();
                        amazonPromiseResolve();
                        scope.$digest();
                    });
                    it("should call backend", function () {
                        expect(mockPlayRoutes.api.controllers.S3Controller.createS3FileMetaData().post).toHaveBeenCalled();
                        expect(fileIdResult).toBe('aFileId');
                    });
                });
                describe('when call to s3 fails', function () {
                    beforeEach(function () {
                        amazonPromiseError = true;
                        promiseResponse = {
                            data: {
                                fileId: 'aFileId',
                                accessKey: 'AJFLWG**E9)',
                                policy: 'lkjdsflkjjelejedsfefe=',
                                signature: '9lkfdjlf983=='
                            }
                        };
                        s3fileService.createFile('akdjf9030f3==').then(function (fileId) {
                            fileIdResult = fileId;
                        });
                        scope.$digest();
                        amazonPromiseResolve();
                        scope.$digest();
                    });
                    it("should call backend and delete the created file", function () {
                        expect(mockPlayRoutes.api.controllers.S3Controller.createS3FileMetaData().post).toHaveBeenCalled();
                        expect(mockPlayRoutes.api.controllers.S3Controller.deleteS3FileMetaData.mostRecentCall.args[0]).toBe('aFileId');
                        expect(mockPlayRoutes.api.controllers.S3Controller.deleteS3FileMetaData().delete).toHaveBeenCalled();
                        expect(fileIdResult).toBe(null);
                    });
                });

            });

        });

        describe('when updating an existing s3file', function () {
            var s3FileId = "aFileId";
            var fileIdResult = null;
            beforeEach(function () {
                fileIdResult = null;
            });
            describe('when permission is denied', function () {
                var calledFailure = false;
                beforeEach(function () {
                    promiseError = true;
                    s3fileService.updateFile(s3FileId, 'akdjf9030f3==').then(function (fileId) {
                            fileIdResult = fileId;
                        },
                        function () {
                            calledFailure = true;
                        });
                    scope.$digest();
                });
                it("should call backend", function () {
                    expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData.mostRecentCall.args[0]).toBe(s3FileId);
                    expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData().put).toHaveBeenCalled();
                });
                it("should have call failure", function () {
                    expect(calledFailure).toBeTruthy();
                });
            });

            describe('when permission is successful', function () {
                beforeEach(function () {
                    promiseError = false;
                });
                describe('when call to S3 post is successful', function () {
                    beforeEach(function () {
                        amazonPromiseError = false;
                        promiseResponse = {
                            data: {
                                fileId: 'aFileId',
                                accessKey: 'AJFLWG**E9)',
                                policy: 'lkjdsflkjjelejedsfefe=',
                                signature: '9lkfdjlf983=='
                            }
                        };
                        s3fileService.updateFile(s3FileId, 'akdjf9030f3==').then(function (fileId) {
                            fileIdResult = fileId;
                        });
                        scope.$digest();
                        amazonPromiseResolve();
                        scope.$digest();
                    });
                    it("should call backend", function () {
                        expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData.mostRecentCall.args[0]).toBe(s3FileId);
                        expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData().put).toHaveBeenCalled();
                        expect(fileIdResult).toBe(s3FileId);
                    });
                });
                describe('when call to S3 post fails', function () {
                    beforeEach(function () {
                        amazonPromiseError = true;
                        promiseResponse = {
                            data: {
                                fileId: 'aFileId',
                                accessKey: 'AJFLWG**E9)',
                                policy: 'lkjdsflkjjelejedsfefe=',
                                signature: '9lkfdjlf983=='
                            }
                        };
                        s3fileService.updateFile(s3FileId, 'akdjf9030f3==').then(function (fileId) {
                            fileIdResult = fileId;
                        });
                        scope.$digest();
                        amazonPromiseResolve();
                        scope.$digest();
                    });
                    it("should call backend", function () {
                        expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData.mostRecentCall.args[0]).toBe(s3FileId);
                        expect(mockPlayRoutes.api.controllers.S3Controller.updateS3FileMetaData().put).toHaveBeenCalled();
                        expect(fileIdResult).toBe(null);
                    });
                });

            });
        });
    });
});