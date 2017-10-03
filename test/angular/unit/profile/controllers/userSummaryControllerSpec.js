define(['angular-mock', 'profile'], function () {
    describe("profileUserSummaryController", function () {
        var userService, s3fileService, mockWin, scope, controller, profileController, q, deferred, promiseResponse, promiseError;
        beforeEach(function () {
            module('profile.controllers.profileUserSummary');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller, $q) {
                scope = $rootScope.$new();
                q = $q;
                userService = {
                    getPrivateInfo: function () {
                        deferred = q.defer();
                         if (promiseError) {
                            deferred.reject(promiseResponse);
                         } else {
                            deferred.resolve(promiseResponse);
                         }
                         return deferred.promise;
                    },
                    updatePrivateProfile: function (a) {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(userService, 'getPrivateInfo').andCallThrough();
                spyOn(userService, 'updatePrivateProfile').andCallThrough();
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
        });

        describe('initialize profile controller', function () {
            beforeEach(function () {
                scope.form = {'$dirty': false };
            });
            describe('when user has profile pic', function () {
                beforeEach(function () {
                    scope.$parent = {
                        user: {
                            profilePic : 'some-fileId'
                        }
                    };
                    controller('ProfileUserSummaryCtrl', {'$scope': scope, 'userService': userService, 's3fileService': s3fileService, '$window': mockWin});
                    scope.$digest();
                });
                it('should set the thumbnail using the id', function () {
                    expect(scope.thumbnail).toBe('http://s3.amazon.com/some-fileId.jpg');
                });
            });
            describe('when user does not have profile pic', function () {
                beforeEach(function () {
                    scope.$parent = {
                        user: {
                            profilePic : null
                        }
                    };
                    controller('ProfileUserSummaryCtrl', {'$scope': scope, 'userService': userService, 's3fileService': s3fileService, '$window': mockWin});
                    scope.$digest();
                });
                it('should set the thumbnail as anonymous', function () {
                    expect(scope.thumbnail).toBe('/assets/img/profile/anon_person.png');
                });
            });
        });

        describe('when form send button is pressed and profile picture has changed', function () {
            beforeEach(function () {
                promiseError = false;
                scope.form = {'$dirty': false };
            });
            describe('when user had a previous profile picture', function () {
                beforeEach(function () {
                    scope.$parent = {
                        user: {
                            profilePic : 'some-fileId'
                        }
                    };
                    controller('ProfileUserSummaryCtrl', {'$scope': scope, 'userService': userService, 's3fileService': s3fileService, '$window': mockWin});
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
            describe('when user had no previous profile picture', function () {
                beforeEach(function () {
                    scope.$parent = {
                        user: {
                            profilePic : null
                        }
                    };
                    controller('ProfileUserSummaryCtrl', {'$scope': scope, 'userService': userService, 's3fileService': s3fileService, '$window': mockWin});
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

        describe('when form send button is pressed and user form has changed', function () {
            beforeEach(function () {
                promiseError = false;
                scope.$parent = {
                    user: {
                        name: 'hello',
                        email: 'original@example.com'
                    }
                };
                scope.form = {
                    '$dirty': false
                };
                controller('ProfileUserSummaryCtrl', {'$scope': scope, 'userService': userService, 's3fileService': s3fileService, '$window': mockWin});
                scope.$digest();
                promiseResponse = {};
                scope.photoHasChanged = false;
                scope.form = {
                    '$dirty': true,
                    'registerName': {
                        '$dirty': true
                    },
                    'registerEmail': {
                        '$dirty': true
                    }
                };

                scope.user = {
                    name: 'aName',
                    email: 'anEmail@example.com'
                }

                scope.submit();
                scope.$digest();
            });
            it('should call userService file', function () {
                expect(userService.updatePrivateProfile).toHaveBeenCalled();
                expect(userService.updatePrivateProfile.mostRecentCall.args[0].name).toBe('aName');
                expect(userService.updatePrivateProfile.mostRecentCall.args[0].email).toBe('anEmail@example.com');
            })

        });
    });
});