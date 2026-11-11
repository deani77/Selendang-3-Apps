import Foundation

// Shared by snapshot downloads and the retained production synchronization path.
private final class EstateNoRedirect: NSObject, URLSessionTaskDelegate {
    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse,
                    newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) {
        completionHandler(nil)
    }
}

enum EstateSyncTransport {
    static func makeSession() -> URLSession {
        URLSession(configuration: .ephemeral, delegate: EstateNoRedirect(), delegateQueue: nil)
    }
}

extension EstateSyncEndpoint {
    var production: URL {
        snapshot.deletingLastPathComponent().appendingPathComponent("production")
    }
}
