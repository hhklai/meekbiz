define(['angular-mock', 'profile'], function () {
    describe("profileController", function () {
        var userService, mizService, mockWin, isLoggedIn, scope, controller, q, deferred, promiseResponse, promiseError, playRoutes, deleteMizResponse;
        beforeEach(function () {
            module('profile.controllers.profile');
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
                    isLoggedIn: function () {
                        return isLoggedIn;
                    }
                };
                spyOn(userService, 'getPrivateInfo').andCallThrough();

                mizService = {
                    getPendingMizes: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    deleteMiz: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(deleteMizResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(mizService, 'getPendingMizes').andCallThrough();
                spyOn(mizService, 'deleteMiz').andCallThrough();

                playRoutes = {
                    view: {
                        controllers: {
                            ProfileController: {
                            }
                        }
                    }
                };

                mockWin = {
                    location: {}
                };
                controller = $controller;

            });
            window.meekbiz = {};
            window.meekbiz.settings = {};
            window.meekbiz.settings.s3host = "http://s3.amazon.com/";
        });

        describe('initialize profile nav controller', function () {
            describe('when logged in', function () {
                beforeEach(function () {
                    promiseError = false;
                });
                describe("when initializing", function () {
                    beforeEach(function () {
                        promiseResponse = {'email':'something@seomthing', 'name':'something'};
                        controller('ProfileCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                        scope.$digest();
                    });
                    it('should call mizService and put it in scope', function () {
                        expect(userService.getPrivateInfo).toHaveBeenCalled();
                        expect(scope.user).toBe(promiseResponse)
                    });
                    it('should call mizService get all Miz', function () {
                        expect(mizService.getPendingMizes).toHaveBeenCalled();
                    });
                });
                describe('when log out button is pressed', function () {
                    beforeEach(function () {
                        promiseResponse = {profilePic: null};
                        controller('ProfileCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                        scope.$digest();
                        isLoggedIn = false;
                        scope.$digest();
                    });
                    it('should redirect', function () {
                        expect(mockWin.location.href).toBe('/');
                    });
                });

            });
            describe('when not logged in', function () {
                beforeEach(function () {
                    promiseError = true;
                    promiseResponse = {};
                    controller('ProfileCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes});
                    scope.$digest();
                });
                it('should redirect', function () {
                    expect(mockWin.location.href).toBe('/');
                });
            });

        });

        describe('delete miz is clicked', function () {
            beforeEach(function () {
                promiseError = false;
                deleteMizResponse = {};
                promiseResponse = [{title: 'aTitle'}, {title: 'something else'}];  // mizService getPending response,
                controller('ProfileCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin, 'playRoutes': playRoutes, '$q': q});
                scope.deleteMiz('aUsername', 'aTitle');
                scope.$digest();
            });
            it("should call mizService", function () {
                expect(mizService.deleteMiz).toHaveBeenCalled();
            });
            it('should delete pending miz when deleteMiz call is successful', function () {
                expect(scope.pendingMizes.length).toBe(1);  //started with 2, now has 1
            });
        });

    });
});