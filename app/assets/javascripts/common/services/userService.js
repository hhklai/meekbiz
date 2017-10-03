define(['angular', '../lib/cookieFactory', '../lib/playRoutes'], function (angular) {
    'use strict';
    var mod = angular.module('common.services.user', ['common.lib.cookie','common.lib.playRoutes']);
    mod.service('userService', ['cookieFactory', 'playRoutes', '$q',
        function (cookieFactory, playRoutes, $q) {

        var self = this;
        self.username = null;

        self.registerUser = function (user) {
            var deferred = $q.defer();
            playRoutes.api.controllers.UserController.registerUser()
                .post(user).then(function (response) {
                    cookieFactory.setCookie('XSRF-TOKEN', response.data.token);
                    self.username = response.data.username;
                    deferred.resolve();
                 }, function () {
                    deferred.reject();
                });
            return deferred.promise;
        };

        self.isLoggedIn = function () {
            return !!self.username;
        }

        self.getUserName =  function () {
            return self.username;
        }

        self.loginUser = function (login) {
            var deferred = $q.defer();
            var successCallback;
            if (login.rememberMe) {
                successCallback = function (response) {
                    self.username = response.data.username;
                    cookieFactory.setCookie('XSRF-TOKEN', response.data.token, {expires: response.data.expiryDays});
                    deferred.resolve();
                }
            } else {
                successCallback = function (response) {
                    self.username = response.data.username;
                    cookieFactory.setCookie('XSRF-TOKEN', response.data.token);
                    deferred.resolve();
                }
            }
            playRoutes.api.controllers.UserController.login().post(login).then(successCallback, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        self.checkIfLoggedIn = function () {
            if (!!self.isLoggedIn()) {
                return true;
            }
            if (!!cookieFactory.getCookie('XSRF-TOKEN')) {
                playRoutes.api.controllers.UserController.viewPrivateProfile('name').get().then(
                    function (response) {
                        self.username = response.data.user.name;
                    },
                    function (reason) {
                        self.username = null;
                        cookieFactory.deleteCookie('XSRF-TOKEN');
                    }
                );
                return true;
            }
            return false;
        };

        self.getPrivateInfo = function () {
            var deferred = $q.defer();
            if (self.user) {
                deferred.resolve(self.user);
            }
            playRoutes.api.controllers.UserController.viewPrivateProfile('all').get().then(
                function (response) {
                    self.user = response.data.user;
                    deferred.resolve(self.user);
                },
                function (reason) {
                    deferred.reject();
                }
            );
            return deferred.promise;
        };

        self.updatePrivateProfile = function (user) {
            var deferred = $q.defer();
            playRoutes.api.controllers.UserController.updatePrivateProfile().patch(user).then(
                function (response) {
                    self.user = response.data;
                    if (!!user.name) {
                        self.username = response.data.name;
                    }
                    deferred.resolve(self.user);
                },
                function (reason) {
                    deferred.reject();
                }
            )
            return deferred.promise;
        }

        self.checkIfEmailExists = function (email) {
            return playRoutes.api.controllers.UserController.checkIfUserExists(email, null).get();
        };

        self.checkIfNameExists = function (name) {
            return playRoutes.api.controllers.UserController.checkIfUserExists(null, name).get();
        };

        self.logout = function () {
            playRoutes.api.controllers.UserController.logout().post().then(function () {
                self.username = null;
                cookieFactory.deleteCookie('XSRF-TOKEN');
            });
        }


    }]);
    return mod;
});