define(['angular-mock', 'common'], function () {
    describe("userService", function () {
        var mockPlayRoutes, mockCookieFactory, userService,scope, q, deferred, promiseResponse, promiseError;
        beforeEach(function () {
            module('common.services.user', function ($provide) {
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
                    }
                };
                spyOn(mockHttp, 'post').andCallThrough();
                spyOn(mockHttp, 'patch').andCallThrough();

                mockPlayRoutes = {
                    api: {
                        controllers: {
                            UserController: {
                                registerUser: function () {
                                    return mockHttp;
                                },
                                login: function () {
                                    return mockHttp;
                                },
                                checkIfUserExists: function (email, name) {
                                    return mockHttp;
                                },
                                viewPrivateProfile: function (name) {
                                    return mockHttp;
                                },
                                updatePrivateProfile: function () {
                                    return mockHttp;
                                },
                                logout: function () {
                                    return mockHttp;
                                }
                            }
                        }
                    }
                };
                spyOn(mockPlayRoutes.api.controllers.UserController, 'checkIfUserExists').andCallThrough();
                spyOn(mockPlayRoutes.api.controllers.UserController, 'viewPrivateProfile').andCallThrough();

                mockCookieFactory = {
                    setCookie: function () {},
                    deleteCookie: function () {}
                };
                spyOn(mockCookieFactory, 'setCookie').andCallThrough();
                spyOn(mockCookieFactory, 'deleteCookie').andCallThrough();

                $provide.value('playRoutes', mockPlayRoutes);
                $provide.value('cookieFactory', mockCookieFactory);
            });
        });
        beforeEach(function () {
            inject(function (_userService_, $rootScope, $q) {
                scope =  $rootScope;
                q = $q;
                userService = _userService_;
            });
            promiseError = false;
            promiseResponse = {};
        });

        describe('when registering a user', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'token': 'atoken'}};
                userService.registerUser({'email':'hello@example.com','name':'hello','password':'password'});
                scope.$digest();
            });
            it("should call backend and set cookie with the generated session token", function () {
                expect(mockPlayRoutes.api.controllers.UserController.registerUser().post).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.UserController.registerUser().post.mostRecentCall.args[0].email).toBe('hello@example.com');
                expect(mockPlayRoutes.api.controllers.UserController.registerUser().post.mostRecentCall.args[0].name).toBe('hello');
                expect(mockPlayRoutes.api.controllers.UserController.registerUser().post.mostRecentCall.args[0].password).toBe('password');
                expect(mockCookieFactory.setCookie).toHaveBeenCalled();
                expect(mockCookieFactory.setCookie.mostRecentCall.args[0]).toBe('XSRF-TOKEN');
                expect(mockCookieFactory.setCookie.mostRecentCall.args[1]).toBe('atoken');
            });
        });

        describe('when logging in a user', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'token': 'atoken', 'expiryDays':2}};
                userService.loginUser({'emailplus':'hello@example.com','password':'password','rememberMe':true});
                scope.$digest();
            });
            it("should call backend and set cookie with the generated session token", function () {
                expect(mockPlayRoutes.api.controllers.UserController.login().post).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.UserController.login().post.mostRecentCall.args[0].emailplus).toBe('hello@example.com');
                expect(mockPlayRoutes.api.controllers.UserController.login().post.mostRecentCall.args[0].password).toBe('password');
                expect(mockPlayRoutes.api.controllers.UserController.login().post.mostRecentCall.args[0].rememberMe).toBe(true);
                expect(mockCookieFactory.setCookie).toHaveBeenCalled();
                expect(mockCookieFactory.setCookie.mostRecentCall.args[0]).toBe('XSRF-TOKEN');
                expect(mockCookieFactory.setCookie.mostRecentCall.args[1]).toBe('atoken');
                expect(mockCookieFactory.setCookie.mostRecentCall.args[2].expires).toBe(2);
            });
        });

        describe('when checking an email', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'found': false}};
                userService.checkIfEmailExists('hello@example.com');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.UserController.checkIfUserExists).toHaveBeenCalled();

            });
        });

        describe('when checking a name', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'found': false}};
                userService.checkIfNameExists('hello');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.UserController.checkIfUserExists).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.UserController.checkIfUserExists.mostRecentCall.args[1]).toBe('hello');
            });
        });

        describe('when visiting the website for the first time and checking if user is logged in', function () {
            describe('when user has loggedIn info is cached on browser some how', function () {
                beforeEach(function () {
                    userService.username = "something";
                });
                it('should return logged in status', function () {
                    userService.checkIfLoggedIn();
                    expect(userService.isLoggedIn()).toBeTruthy();
                });
            });

            describe('when visiting the website with a remembered session that has been invalidated on server side', function () {
                beforeEach(function () {
                    mockCookieFactory.getCookie = function (token) {
                        return 'something';
                    };
                    promiseError = true;
                    userService.checkIfLoggedIn();
                    scope.$digest();
                });
                it('should call backend to check if session is valid and delete it', function () {
                    expect(mockPlayRoutes.api.controllers.UserController.viewPrivateProfile).toHaveBeenCalled();
                    expect(userService.isLoggedIn()).toBeFalsy();
                    expect(mockCookieFactory.deleteCookie).toHaveBeenCalled();
                    expect(mockCookieFactory.deleteCookie.mostRecentCall.args[0]).toBe('XSRF-TOKEN');
                });
            });
        });

        describe('when log out has been clicked', function () {
            beforeEach(function () {
                userService.logout();
                scope.$digest();
            });
            it('should call backend and delete cookie', function () {
                expect(mockPlayRoutes.api.controllers.UserController.logout().post).toHaveBeenCalled();
                expect(mockCookieFactory.deleteCookie).toHaveBeenCalled();
                expect(mockCookieFactory.deleteCookie.mostRecentCall.args[0]).toBe('XSRF-TOKEN');
            });

        });

        describe('when checking a name', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'found': false}};
                userService.checkIfNameExists('hello');
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.UserController.checkIfUserExists).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.UserController.checkIfUserExists.mostRecentCall.args[1]).toBe('hello');
            });
        });

        describe('when updating an attribute in user profile page', function () {
            beforeEach(function () {
                promiseResponse = {'data': {'found': false}};
                userService.updatePrivateProfile({'profilePic': 'aFileId'});
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.UserController.updatePrivateProfile().patch).toHaveBeenCalled();
                expect(mockPlayRoutes.api.controllers.UserController.updatePrivateProfile().patch.mostRecentCall.args[0].profilePic).toBe('aFileId');
            });
        });
    });
});