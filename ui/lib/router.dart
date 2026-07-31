import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:ui/services/api_client.dart';
import 'package:ui/util/app_storage.dart';
import 'package:ui/views/files/file_view.dart';
import 'package:ui/views/files/shared_view.dart';
import 'package:ui/views/files/starred_view.dart';
import 'package:ui/views/home/home_view.dart';
import 'package:ui/views/setup/login_view.dart';
import 'package:ui/views/setup/server_setup_view.dart';
import 'package:ui/views/setup/setup_view.dart';

part 'router.g.dart';

@riverpod
GoRouter goRouter(Ref ref) {
  final goRouter = GoRouter(
    routes: [
      StatefulShellRoute.indexedStack(
        builder: (_, state, navigationShell) =>
            HomeView(navigationShell: navigationShell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(path: '/files', builder: (_, _) => const FileView()),
              GoRoute(
                path: '/folders/:folderId',
                pageBuilder: (context, state) {
                  var folderId = state.pathParameters['folderId'];

                  return CustomTransitionPage(
                    key: state.pageKey,
                    transitionsBuilder: (context, animation, _, child) {
                      return FadeTransition(
                        opacity: CurveTween(
                          curve: Curves.easeInOutCirc,
                        ).animate(animation),
                        child: child,
                      );
                    },
                    child: FileView(parentId: folderId),
                  );
                },
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/starred',
                builder: (context, _) => const StarredView(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/shared',
                builder: (context, _) => const SharedView(),
              ),
            ],
          ),
        ],
      ),
      ShellRoute(
        builder: (_, _, child) => SetupView(child: child),
        routes: [
          GoRoute(
            path: '/setup',
            builder: (_, _) => ServerSetupView(),
            redirect: (_, _) async {
              if (kIsWeb || (await AppStorage.serverAddress) != null) {
                return '/login';
              }

              return null;
            },
          ),
          GoRoute(
            path: '/login',
            builder: (_, _) => LoginView(),
            redirect: (context, _) async {
              if ((await AppStorage.username) != null && context.mounted) {
                var apiClient = ref.read(apiClientProvider);
                bool tokenRefreshed = await apiClient.refreshToken();

                return tokenRefreshed ? '/files' : null;
              }

              return null;
            },
          ),
        ],
      ),
      GoRoute(path: '/:catchAll(.*)', redirect: (_, _) => '/files'),
    ],
    redirect: (context, _) async {
      if (!kIsWeb) {
        var serverAddress = await AppStorage.serverAddress;

        if (serverAddress == null) {
          await AppStorage.clearRememberMe();
          await AppStorage.clearUsername();

          return '/setup';
        }
      }

      var username = await AppStorage.username;

      if (username == null) {
        await AppStorage.clearRememberMe();

        return '/login';
      }

      return null;
    },
    initialLocation: '/login',
  );

  ref.onDispose(() => goRouter.dispose());

  return goRouter;
}
