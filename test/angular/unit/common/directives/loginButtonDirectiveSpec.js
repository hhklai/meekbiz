define(['angular-mock','common'], function () {
    describe("loginDirective", function () {
        var element, $scope, modalMock;
        beforeEach(function () {
            module('common.directives.loginButton');
        });
        beforeEach(function () {
            inject(function ($compile, $rootScope, $modal) {
                $scope = $rootScope;
                element =  angular.element('<login-button>Sign in</login-button>');
                $compile(element)($rootScope);

                modalMock = $modal;
                spyOn(modalMock, 'open');
            })
        });

        describe('when clicking on login button', function () {
            it("should open a modal", function () {
                element.triggerHandler('click');
                expect(modalMock.open).toHaveBeenCalled();
                expect(modalMock.open.mostRecentCall.args[0].templateUrl).toBe('/assets/partials/common/loginModal.html');
            });
        });

        describe('when click okay in loginModal', function () {
            var controller, modalInstance, userService, deferred;
            beforeEach(function () {
                inject(function ($controller, $q) {
                    deferred = $q.defer();
                    modalInstance = {
                        'open': jasmine.createSpy(),
                        'close': jasmine.createSpy()
                    };
                    userService = {
                        'loginUser': function () {
                            return deferred.promise;
                        }
                    };
                    spyOn(userService, 'loginUser').andCallThrough();
                    controller = $controller('loginModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService });
                    $scope.ok({'email':'email','password':'password'});
                    deferred.resolve();
                });
            });
            it("should call userService login", function () {
                expect(userService.loginUser).toHaveBeenCalled();
                expect(userService.loginUser.mostRecentCall.args[0].email).toBe('email');
                expect(userService.loginUser.mostRecentCall.args[0].password).toBe('password');
            })
        });
    });
});