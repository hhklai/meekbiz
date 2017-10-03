define(['angular-mock', 'common'], function () {
    describe("managementService", function () {
        var mockPlayRoutes, managementService, scope, q, deferred, promiseResponse, promiseError;
        beforeEach(function () {
            module('common.services.management', function ($provide) {
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
                    put: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    delete: function () {
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
                spyOn(mockHttp, 'put').andCallThrough();
                spyOn(mockHttp, 'delete').andCallThrough();

                mockPlayRoutes = {
                    api: {
                        controllers: {
                            SearchController: {
                                reInitSearch: function () {
                                    return mockHttp;
                                },
                                synchSearchIndexWithNewMizes: function () {
                                    return mockHttp;
                                },
                                rebuildSearchIndex: function () {
                                    return mockHttp;
                                }
                            }
                        }
                    }
                };

                $provide.value('playRoutes', mockPlayRoutes);
            });
        });
        beforeEach(function () {
            inject(function (_managementService_, $rootScope, $q) {
                scope =  $rootScope;
                q = $q;
                managementService = _managementService_;
            });
            promiseError = false;
            promiseResponse = {};
        });

        describe('when reinit search called', function () {
            beforeEach(function () {
                managementService.reInitSearch();
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.SearchController.reInitSearch().put).toHaveBeenCalled();
            });
        });

        describe('when ingest now search called', function () {
            beforeEach(function () {
                managementService.synchWithNewMizes();
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.SearchController.synchSearchIndexWithNewMizes().put).toHaveBeenCalled();
            });
        });

        describe('when rebuild search index called', function () {
            beforeEach(function () {
                managementService.rebuildSearchIndex();
                scope.$digest();
            });
            it("should call backend", function () {
                expect(mockPlayRoutes.api.controllers.SearchController.rebuildSearchIndex().put).toHaveBeenCalled();
            });
        });
    });
});