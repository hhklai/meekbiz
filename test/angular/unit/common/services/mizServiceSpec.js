define(['angular-mock', 'common'], function () {
    describe("mizService", function () {
        var mockPlayRoutes, mizService, mockS3fileService, scope, q, deferred, promiseResponse, promiseError, s3PromiseResponse;
        beforeEach(function () {
            module('common.services.miz', function ($provide) {
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
                    patch: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    get: function () {
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
                spyOn(mockHttp, 'patch').andCallThrough();
                spyOn(mockHttp, 'delete').andCallThrough();

                var s3http = {
                    post: function () {
                        deferred = q.defer();
                        deferred.resolve(s3PromiseResponse);
                        return deferred.promise;
                    }
                };
                spyOn(s3http, 'post').andCallThrough();

                mockPlayRoutes = {
                    api: {
                        controllers: {
                            MizController: {
                                checkIfMizExists: function (title) {
                                    return mockHttp;
                                },
                                createMiz: function (miz) {
                                    return mockHttp;
                                },
                                getPrivateMiz : function () {
                                    return mockHttp;
                                },
                                getPendingMizes: function () {
                                    return mockHttp;
                                },
                                updateMiz: function () {
                                    return mockHttp;
                                },
                                requestS3Ids: function () {
                                    return s3http;
                                },
                                deleteMizThatHasNeverBeenActivated: function (username, title) {
                                    return mockHttp;
                                }

                            }
                        }
                    }
                };
                spyOn(mockPlayRoutes.api.controllers.MizController, 'checkIfMizExists').andCallThrough();
                spyOn(mockPlayRoutes.api.controllers.MizController, 'getPrivateMiz').andCallThrough();
                spyOn(mockPlayRoutes.api.controllers.MizController, 'getPendingMizes').andCallThrough();
                spyOn(mockPlayRoutes.api.controllers.MizController, 'deleteMizThatHasNeverBeenActivated').andCallThrough();

                mockS3fileService = {
                    'updateS3' : jasmine.createSpy()
                };

                $provide.value('playRoutes', mockPlayRoutes);
                $provide.value('s3fileService', mockS3fileService);
            });
        });
        beforeEach(function () {
            inject(function (_mizService_, $rootScope, $q) {
                scope =  $rootScope;
                q = $q;
                mizService = _mizService_;
            });
            promiseError = false;
            promiseResponse = {};
        });

        describe('when creating a new miz', function () {
            beforeEach(function () {
                promiseResponse = {'data': 'mizTitle'};
                mizService.createMiz({'title':'hello','summary':'hello world'});
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.createMiz().post).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.MizController.createMiz().post.mostRecentCall.args[0].title).toBe('hello');
                expect(mockPlayRoutes.api.controllers.MizController.createMiz().post.mostRecentCall.args[0].summary).toBe('hello world');
            });
        });

        describe('when miz title exists', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'found': false}};
                mizService.checkIfMizExists('hello');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.checkIfMizExists).toHaveBeenCalled();
            });
        });

        describe('when getting private Miz info', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'title': 'aTitle', 'summary': 'aDescription'}};
                mizService.getPrivateMiz('aUsername', 'aTitle');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.getPrivateMiz).toHaveBeenCalled();
            });
        });

        describe('when getting all Miz', function () {
            beforeEach(function () {
                promiseResponse = {'data': [
                    {'title': 'aTitle', 'summary': 'aDescription'},
                    {'title': 'aTitle2', 'summary': 'aDescription something else'}
                ]};
                mizService.getPendingMizes();
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.getPendingMizes).toHaveBeenCalled();
            });
        });

        describe('when updating a miz', function () {
            beforeEach(function () {
                promiseResponse = {'data': [
                    {'title': 'aTitle', 'summary': 'aDescription'}
                ]};
                mizService.updateMiz('some Miz',{});
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.updateMiz('some Miz').post).toHaveBeenCalled();
            });
        });

        describe('when updating a miz description with img', function () {
            var guidObj1, guidObj2, miz;
            beforeEach(function () {
                window.meekbiz = {};
                window.meekbiz.settings = {};
                window.meekbiz.settings.s3host = "http://img-dev.meekbiz.com/";
                guidObj1 = {'fileId':'a-guid-1'};
                guidObj2 = {'fileId':'a-guid-2'};
                s3PromiseResponse = {'data': [  //response for requestS3Ids
                    guidObj1,
                    guidObj2
                ]};
                miz = {
                    contentBody: '<img src="data:image/jpeg;base64,aeig84jddglijf=="/><img src="data:image/jpeg;base64,a983h6jddglijf=="/>'
                }
                mizService.updateMiz('some Miz', miz);
                scope.$digest();
            });

            it("should request s3ids from backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.requestS3Ids('some Miz').post).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.MizController.requestS3Ids().post.mostRecentCall.args[0].numIds).toBe(2);  //grab ids from server
            });
            it("should upload data to amazon s3", function () {
                expect(mockS3fileService.updateS3).toHaveBeenCalledWith('data:image/jpeg;base64,aeig84jddglijf==', guidObj1);  //transfer data to s3 storage
                expect(mockS3fileService.updateS3).toHaveBeenCalledWith('data:image/jpeg;base64,a983h6jddglijf==', guidObj2);  //transfer data to s3 storage
            });
            it("should replace data with miz ids and patch backend", function () {
                expect(miz.contentBody).toBe('<img src="http://img-dev.meekbiz.com/a-guid-1.jpg"/><img src="http://img-dev.meekbiz.com/a-guid-2.jpg"/>');  //have replaced original miz content
                expect(mockPlayRoutes.api.controllers.MizController.updateMiz('some Miz').post).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.MizController.updateMiz().post.mostRecentCall.args[0]).toBe(miz);
            });
        });

        describe('when deleting a miz', function () {
            beforeEach(function () {
                promiseResponse = {};
                mizService.deleteMiz('aUser','some Miz');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.MizController.deleteMizThatHasNeverBeenActivated('aUser','some Miz').delete).toHaveBeenCalled();
            });
        });
    });
});