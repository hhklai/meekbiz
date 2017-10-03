define(['angular-mock', 'common'], function () {
    describe("emailNotTakenDirective", function () {
        var element, $scope, userService, form, q, deferred, promiseResponse;
        beforeEach(function () {
            module('common.directives.forms.validators.emailNotTaken', function ($provide) {
                $provide.decorator('userService', function ($delegate) {
                    $delegate.checkIfEmailExists = function () {
                        deferred = q.defer();
                        deferred.resolve(promiseResponse);
                        return deferred.promise;
                    }
                    spyOn($delegate, 'checkIfEmailExists').andCallThrough();
                    return $delegate;
                });
            });

            inject(function (_userService_) {
                userService = _userService_;
            });
        });

        describe('when email not taken directive has no ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="email" id="registerEmail" name="registerEmail" ng-model="user.email" email-not-taken />' +
                        '</form>');
                    $scope.user = { email: null };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });

            describe('when inputting an email, and found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': true}}
                });
                it("should call backend to check and mark invalid", function () {
                    form.registerEmail.$setViewValue('hello@example.com');
                    $scope.$digest();
                    expect(userService.checkIfEmailExists).toHaveBeenCalled();
                    expect(form.registerEmail.$valid).toBe(false);
                });
            });

            describe('when inputting an email, and not found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': false}};
                });
                it("should call backend and mark valid", function () {
                    form.registerEmail.$setViewValue('hello@example.com');
                    $scope.$digest();
                    expect(userService.checkIfEmailExists).toHaveBeenCalled();
                    expect(form.registerEmail.$valid).toBe(true);
                });
            });
        });

        describe('when email not taken directive has ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="email" id="registerEmail" name="registerEmail" ng-model="user.email" email-not-taken="originalUser.email" />' +
                        '</form>');
                    $scope.user = { email: null };
                    $scope.originalUser = { email: "anEmail@example.com" };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });
            it('should call backend if input email doesn\'t match original email', function () {
                promiseResponse = {'data': {'found': false}};
                form.registerEmail.$setViewValue('hello@example.com');
                $scope.$digest();
                expect(userService.checkIfEmailExists).toHaveBeenCalled();
                expect(form.registerEmail.$valid).toBe(true);
            });
            it('should return valid if input email matches original email', function () {
                form.registerEmail.$setViewValue('anEmail@example.com');
                expect(userService.checkIfEmailExists).not.toHaveBeenCalled();
                expect(form.registerEmail.$valid).toBe(true);
            });
        });
    });
});