define(['angular-mock', 'common'], function () {
    describe("nameNotTakenDirective", function () {
        var element, $scope, userService, form, q, deferred, promiseResponse;
        beforeEach(function () {
            module('common.directives.forms.validators.nameNotTaken', function ($provide) {
                $provide.decorator('userService', function ($delegate) {
                    $delegate.checkIfNameExists = function () {
                        deferred = q.defer();
                        deferred.resolve(promiseResponse);
                        return deferred.promise;
                    }
                    spyOn($delegate, 'checkIfNameExists').andCallThrough();
                    return $delegate;
                });
            });

            inject(function (_userService_) {
                userService = _userService_;
            });
        });

        describe('when name not taken directive has no ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="text" id="registerName" name="registerName" ng-model="user.name" name-not-taken />' +
                        '</form>');
                    $scope.user = { name: null };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });

            describe('when inputting a name, and found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': true}}
                });
                it("should call backend to check and mark invalid", function () {
                    form.registerName.$setViewValue('hello');
                    $scope.$digest();
                    expect(userService.checkIfNameExists).toHaveBeenCalled();
                    expect(form.registerName.$valid).toBe(false);
                });
            });

            describe('when inputting a name, and not found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': false}};
                });
                it("should call backend and mark valid", function () {
                    form.registerName.$setViewValue('hello');
                    $scope.$digest();
                    expect(userService.checkIfNameExists).toHaveBeenCalled();
                    expect(form.registerName.$valid).toBe(true);
                });
            });
        });

        describe('when name not taken directive has ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="text" id="registerName" name="registerName" ng-model="user.name" name-not-taken="originalUser.name" />' +
                        '</form>');
                    $scope.user = { name: null };
                    $scope.originalUser = { name: "aName" };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });
            it('should call backend if input name doesn\'t match original name', function () {
                promiseResponse = {'data': {'found': false}};
                form.registerName.$setViewValue('hello');
                $scope.$digest();
                expect(userService.checkIfNameExists).toHaveBeenCalled();
                expect(form.registerName.$valid).toBe(true);
            });
            it('should return valid if input name matches original name', function () {
                form.registerName.$setViewValue('aName');
                expect(userService.checkIfNameExists).not.toHaveBeenCalled();
                expect(form.registerName.$valid).toBe(true);
            });
        });

    });
});