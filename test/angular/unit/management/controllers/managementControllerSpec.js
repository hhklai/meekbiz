define(['angular-mock', 'management'], function () {
    describe("managementController", function () {
        var managementService, scope, controller, q, deferred, promiseResponse, promiseError;
        beforeEach(function () {
            module('management.controllers.management');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller, $q) {
                scope = $rootScope.$new();
                q = $q;
                managementService = {
                    reInitSearch : function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    synchWithNewMizes: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    rebuildSearchIndex: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(managementService, 'reInitSearch').andCallThrough();
                spyOn(managementService, 'synchWithNewMizes').andCallThrough();
                spyOn(managementService, 'rebuildSearchIndex').andCallThrough();

                controller = $controller;
            });
        });

        describe('when reconnectToSearch button is pressed', function () {
            beforeEach(function () {
                promiseError = false;
                controller('ManagementCtrl', {'$scope': scope, 'managementService': managementService});
                scope.$digest();
                promiseResponse = {};
                scope.reconnectToSearch();
                scope.$digest();
            });
            it('should call managementService', function () {
                expect(managementService.reInitSearch).toHaveBeenCalled();
            })
        });

        describe('when ingestNewMizNow button is pressed', function () {
            beforeEach(function () {
                promiseError = false;
                controller('ManagementCtrl', {'$scope': scope, 'managementService': managementService});
                scope.$digest();
                promiseResponse = {};
                scope.ingestNewMizNow();
                scope.$digest();
            });
            it('should call managementService', function () {
                expect(managementService.synchWithNewMizes).toHaveBeenCalled();
            })
        });

        describe('when deleteSearchAndReingestAll button is pressed', function () {
            beforeEach(function () {
                promiseError = false;
                controller('ManagementCtrl', {'$scope': scope, 'managementService': managementService});
                scope.$digest();
                promiseResponse = {};
                scope.deleteSearchAndReingestAll();
                scope.$digest();
            });
            it('should call managementService', function () {
                expect(managementService.rebuildSearchIndex).toHaveBeenCalled();
            })
        });
    });
});