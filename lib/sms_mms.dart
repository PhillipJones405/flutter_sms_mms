import 'sms_mms_platform_interface.dart';
//
// class SmsMms {
//
//   static Future<void> send({
//     required List<String> recipients,
//     List<String>? filePath,
//     required String message,
//   }) async {
//     print('argument');
//     return SmsMmsPlatform.instance.sendMms(
//       phones: recipients,
//       text: message,
//       filePath: filePath,
//     );
//   }
// }

import 'package:flutter/services.dart';

class SmsMms {
  static const MethodChannel _channel = MethodChannel('sms_mms');

  static Future<void> sendMms(List<String> paths, List<String> recipientNumbers, String message) async {
    try {
      await _channel.invokeMethod('sendMms', {
        'paths': paths,
        'recipientNumbers': recipientNumbers,
        'message': message,
      });
    } on PlatformException catch (e) {
      print("Failed to send MMS: '${e.message}'.");
    }
  }
}

