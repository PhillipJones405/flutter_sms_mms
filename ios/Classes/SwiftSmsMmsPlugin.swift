import Flutter
import UIKit
import MessageUI

public class SmsMmsPlugin: NSObject, FlutterPlugin, MFMessageComposeViewControllerDelegate {
  var result: FlutterResult?

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "sms_mms", binaryMessenger: registrar.messenger())
    let instance = SmsMmsPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method == "sendMms" {
    print("in public func handle")
      guard let args = call.arguments as? [String: Any],
            let paths = args["paths"] as? [String],
            let recipients = args["recipientNumbers"] as? [String],
            let message = args["message"] as? String else {
        result(FlutterError(code: "INVALID_ARGUMENT", message: "Invalid argument", details: nil))
        return
      }
      sendMms(paths: paths, recipients: recipients, message: message, result: result)
    } else {
      result(FlutterMethodNotImplemented)
    }
  }

  private func sendMms(paths: [String], recipients: [String], message: String, result: @escaping FlutterResult) {
    print("in sendMMs")
    if MFMessageComposeViewController.canSendText() {
       print("in canSendText")
      let messageController = MFMessageComposeViewController()
      messageController.messageComposeDelegate = self
      messageController.recipients = recipients
      messageController.body = message
      print("check if can send attachments")
      if MFMessageComposeViewController.canSendAttachments() {
        print("in if to make for loop for paths")
        for path in paths {
          let fileURL = URL(fileURLWithPath: path)
          messageController.addAttachmentURL(fileURL, withAlternateFilename: nil)
        }
      }

      self.result = result
      if let rootViewController = UIApplication.shared.delegate?.window??.rootViewController {
        rootViewController.present(messageController, animated: true, completion: nil)
      }
    } else {
      result(FlutterError(code: "UNAVAILABLE", message: "SMS services are not available", details: nil))
    }
  }

  public func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
    controller.dismiss(animated: true, completion: nil)
    switch result {
    case .sent:
      self.result?(true)
    default:
      self.result?(false)
    }
  }
}
