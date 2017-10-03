define(['angular-mock', 'miz'], function () {
    describe("mizController", function () {
        var isLoggedIn, userService, mizService, mockWin, scope, controller, q, deferred, promiseResponse, promiseError;
        var username = 'hello', title = 'world';
        beforeEach(function () {
            module('miz.controllers.miz');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller, $q) {
                scope = $rootScope.$new();
                q = $q;
                mizService = {
                    getPrivateMiz: function () {
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

                userService = {
                    isLoggedIn: function () {
                        return isLoggedIn;
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
            window.username = username;
            window.mizTitle = title;
        });

        describe('initialize miz controller', function () {
            describe('when logged in', function () {
                beforeEach(function () {
                    promiseError = false;

                });
                describe("when initializing", function () {
                    beforeEach(function () {
                        promiseResponse = {'title':'title', 'summary':'summary'};
                        controller('MizCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin});
                        scope.$digest();
                    });
                    it('should call getPrivateMiz and put it in scope', function () {
                        expect(mizService.getPrivateMiz).toHaveBeenCalled();
                        expect(scope.miz).toBe(promiseResponse);
                    });
                });

                describe('when log out button is pressed', function () {
                    beforeEach(function () {
                        promiseResponse = {thumbnail: null};
                        controller('MizCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin});
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
                    controller('MizCtrl', {'$scope': scope, 'userService': userService, 'mizService': mizService, '$window': mockWin});
                    scope.$digest();
                });
                it('should redirect', function () {
                    expect(mockWin.location.href).toBe('/');
                });
            });
        });
    });
});