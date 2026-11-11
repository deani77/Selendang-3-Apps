import Foundation

let syncEndpoint = try EstateSyncEndpoint(
    address: " https://example.com:9443/v1/estates/selendang-3/snapshot ", token: " example-token ")
precondition(syncEndpoint.production.absoluteString == "https://example.com:9443/v1/estates/selendang-3/production")
precondition(syncEndpoint.token == "example-token")
for address in ["http://example.com/v1/estates/selendang-3/snapshot",
                "https://user:pass@example.com/v1/estates/selendang-3/snapshot",
                "https://example.com/v1/estates/other/snapshot",
                "https://example.com/v1/estates/selendang-3/snapshot?token=x"] {
    do { _ = try EstateSyncEndpoint(address: address, token: "example"); fatalError("Invalid sync endpoint accepted") } catch {}
}
let transportSession = EstateSyncTransport.makeSession()
let transportTask = transportSession.dataTask(with: syncEndpoint.snapshot)
let redirectResponse = HTTPURLResponse(url: syncEndpoint.snapshot, statusCode: 302, httpVersion: nil,
                                      headerFields: ["Location": "https://other.example/"])!
var redirectChecked = false
(transportSession.delegate as! URLSessionTaskDelegate).urlSession?(
    transportSession, task: transportTask, willPerformHTTPRedirection: redirectResponse,
    newRequest: URLRequest(url: URL(string: "https://other.example/")!), completionHandler: { request in
        precondition(request == nil)
        redirectChecked = true
    })
precondition(redirectChecked)
transportTask.cancel()
transportSession.invalidateAndCancel()
print("Shared sync endpoint derivation and redirect refusal checks passed without network requests")
