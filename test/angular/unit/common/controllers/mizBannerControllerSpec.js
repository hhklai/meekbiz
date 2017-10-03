define(['angular-mock', 'common'], function () {
    describe("mizBannerController", function () {
        var s3fileService, mizService, mockWin, scope, controller, q, deferred, promiseResponse, promiseError;
        var username = 'hello', title = 'world';
        beforeEach(function () {
            module('common.controllers.mizBanner');
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
            describe('when miz has bannerPic', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            bannerPic : 'some-fileId'
                        }
                    }
                    controller('MizBannerCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin});
                    scope.$digest();
                });
                it('should set the bannerPic using the id', function () {
                    expect(scope.bannerPic).toBe('http://s3.amazon.com/some-fileId.jpg');
                });
            });
            describe('when user does not have bannerPic', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            bannerPic : null
                        }
                    }
                    controller('MizBannerCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin});
                    promiseResponse = { bannerPic : 'aBanner' };
                    scope.$digest();
                });
                it('should set the banner pic as default', function () {
                    expect(scope.bannerPic).toBe('/assets/img/miz/ninbg.jpg');
                });
            });
        });

        describe('when banner form send button is pressed and banner has changed', function () {
            beforeEach(function () {
                scope.form = {'$dirty': false };
            });
            describe('when miz had a previous banner pic', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            bannerPic : 'some-fileId'
                        }
                    }
                    controller('MizBannerCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin});
                    scope.$digest();
                    scope.bannerHasChanged = true;
                    scope.submitBanner();
                    scope.$digest();
                });
                it('should call update file', function () {
                    expect(s3fileService.updateFile).toHaveBeenCalled();
                })
            });
            describe('when miz had no previous banner picture', function () {
                beforeEach(function () {
                    scope.$parent = {
                        miz: {
                            bannerPic : null
                        }
                    }
                    controller('MizBannerCtrl', {'$scope': scope, 's3fileService': s3fileService, 'mizService': mizService, '$window': mockWin});
                    scope.$digest();
                    scope.bannerHasChanged = true;
                    scope.submitBanner();
                    scope.$digest();
                });
                it('should call create file', function () {
                    expect(s3fileService.createFile).toHaveBeenCalled();
                })
            });
        });
    });
});