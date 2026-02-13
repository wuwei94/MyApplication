import 'package:basic_flutter/state_management/bloc/cubit/my_bloc_cubit.dart';
import 'package:basic_flutter/state_management/bloc/page/my_bloc_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// BloC
/// https://pub.dev/packages/flutter_bloc
class MyBloC extends StatelessWidget {
  const MyBloC({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => MyBloCCubit(),
      child: const MyBloCPage(title: 'BloC'),
    );
  }
}
