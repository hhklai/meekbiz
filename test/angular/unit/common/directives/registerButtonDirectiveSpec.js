define(['angular-mock','common'], function () {
    describe("registerButtonDirective", function () {
        var element, $scope, modalMock;
        beforeEach(function () {
            module('common.directives.registerButton');
        });
        beforeEach(function () {
            inject(function ($compile, $rootScope, $modal) {
                $scope = $rootScope;
                element =  angular.element('<register-button>Join Now</register-button>');
                $compile(element)($rootScope);

                modalMock = $modal;
                spyOn(modalMock, 'open');
            })
        });

        describe('when clicking on register button', function () {
            it("should open a modal", function () {
                element.triggerHandler('click');
                expect(modalMock.open).toHaveBeenCalled();
                expect(modalMock.open.mostRecentCall.args[0].templateUrl).toBe('/assets/partials/common/registerModal.html');
            });
        });

        describe('when click okay in registerModal', function () {
            var controller, modalInstance, userService, deferred;
            beforeEach(function () {
                inject(function ($controller, $q) {
                    deferred = $q.defer();
                    modalInstance = {
                        'open': jasmine.createSpy(),
                        'close': jasmine.createSpy()
                    };
                    userService = {
                        'registerUser': function () {
                            return deferred.promise
                        }
                    };
                    spyOn(userService, 'registerUser').andCallThrough();
                    controller = $controller('registerModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService });

                    $scope.ok({'email':'email','name':'name','password':'password'})
                    deferred.resolve();
                });
            });
            it("should call userService register user", function () {
                expect(userService.registerUser).toHaveBeenCalled();
                expect(userService.registerUser.mostRecentCall.args[0].email).toBe('email');
                expect(userService.registerUser.mostRecentCall.args[0].name).toBe('name');
                expect(userService.registerUser.mostRecentCall.args[0].password).toBe('password');
            })
        });
    });
});