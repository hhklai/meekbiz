define(['angular-mock', 'topnav'], function () {
    describe("topNavController", function () {
        var userService, scope, navController;
        beforeEach(function () {
            module('topnav.controllers.topNav');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller) {
                scope = $rootScope.$new();
                userService = {
                    checkIfLoggedIn: function () {
                    },
                    logout: function () {
                    }
                };
                spyOn(userService, 'checkIfLoggedIn').andCallThrough();
                spyOn(userService, 'logout').andCallThrough();
                navController = $controller('TopNavCtrl', {'$scope': scope, 'userService': userService});
            });
        });

        describe('initialize top nav controller', function () {
            it('should check it user is logged in', function () {
                expect(userService.checkIfLoggedIn).toHaveBeenCalled();
            });
        });

        describe('when logging out', function () {
            beforeEach(function () {
                scope.logout();
            });
            it('should call user service log out', function () {
                expect(userService.logout).toHaveBeenCalled();
            });
        });
    });
});