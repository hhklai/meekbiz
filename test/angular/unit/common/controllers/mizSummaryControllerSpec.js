define(['angular-mock', 'common'], function () {
    describe("mizSummaryController", function () {
        var s3fileService, mizService, mockWin, scope, controller, q, deferred, promiseResponse, promiseError, playRoutes;
        var username = 'hello', title = 'world';
        beforeEach(function () {
            module('common.controllers.mizSummary');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller, $q) {
                scope = $rootScope.$new();
                q = $q;
                mizService = {
                    getPrivateMiz : function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    updateMiz: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(mizService, 'getPrivateMiz').andCallThrough();
                spyOn(mizService, 'updateMiz').andCallThrough();
                s3fileService = {
                    updateFile: function (a, b) {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    createFile: function (a) {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(s3fileService, 'updateFile').andCallThrough();
                spyOn(s3fileService, 'createFile').andCallThrough();

                playRoutes = {
                    view: {
                        controllers: {
                            MizController: {
                                miz: function () {
                                    return {
                                        url: null
                                    }
                                }
                            }
                        }
                    }
                };

                mockWin = {
                    location:{}
                };
                controller = $controller;

            });
            window.meekbiz = {};
            window.meekbiz.settings = {};
            window.meekbiz.settings.s3host = "http://s3.amazon.com/";
            window.username =  username;
            window.mizTitle = title;
        });

        describe('initialize miz controller', function () {
            beforeEach(function () {
                scope.form = {'$dirty': false };
            });
            describe('when miz has thumbnail', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            thumbnail : 'some-fileId'
                        }
                    }
                    controller('MizSummaryCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                    scope.$digest();
                });
                it('should set the thumbnail using the id', function () {
                    expect(scope.thumbnail).toBe('http://s3.amazon.com/some-fileId.jpg');
                });
            });
            describe('when user does not have thumbnail', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            thumbnail : null
                        }
                    }
                    controller('MizSummaryCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                    scope.$digest();
                });
                it('should set the thumbnail as anonymous', function () {
                    expect(scope.thumbnail).toBe('/assets/img/miz/round_question_mark.jpg');
                });
            });
        });

        describe('when form send button is pressed and thumbnail has changed', function () {
            beforeEach(function () {
                promiseError = false;
                scope.form = {'$dirty': false };
            });
            describe('when miz had a previous thumbnail', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            thumbnail : 'some-fileId'
                        }
                    }
                    controller('MizSummaryCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                    scope.$digest();
                    promiseResponse = {};
                    scope.photoHasChanged = true;
                    scope.submit();
                    scope.$digest();
                });
                it('should call update file', function () {
                    expect(s3fileService.updateFile).toHaveBeenCalled();
                })
            });
            describe('when miz had no previous thumbnail picture', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            thumbnail : null
                        }
                    }
                    controller('MizSummaryCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                    scope.$digest();
                    promiseResponse = {};
                    scope.photoHasChanged = true;
                    scope.submit();
                    scope.$digest();
                });
                it('should call create file', function () {
                    expect(s3fileService.createFile).toHaveBeenCalled();
                })
            });
        });

        describe('when form send button is pressed and miz form has changed', function () {
            beforeEach(function () {
                promiseError = false;
                scope.$parent = {
                    miz: {
                        title: 'hello1',
                        summary: 'world1',
                        profilePic : null
                    }
                };
                scope.form = {'$dirty': false };
                controller('MizSummaryCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                scope.$digest();
                promiseResponse = {};
                scope.photoHasChanged = false;
                scope.form = {
                    '$dirty': true,
                    'title': {
                        '$dirty': true
                    },
                    'summary': {
                        '$dirty': true
                    }
                };

                scope.miz = {
                    title: 'hello1',
                    summary: 'world1'
                }

                scope.submit();
                scope.$digest();
            });
            it('should call mizService', function () {
                expect(mizService.updateMiz).toHaveBeenCalled();
                expect(mizService.updateMiz.mostRecentCall.args[0]).toBe(title);
                expect(mizService.updateMiz.mostRecentCall.args[1].title).toBe('hello1');
                expect(mizService.updateMiz.mostRecentCall.args[1].summary).toBe('world1');
            })

        });
    });
});