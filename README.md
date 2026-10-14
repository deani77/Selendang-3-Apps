# Ladang Selendang 3

Initial Android home-screen design reference for **v0.1 Alpha**.

This repository currently records the approved design only. No Android application, working navigation, database, synchronization service, or APK has been built.

## Approved home screen

- **Title:** Ladang Selendang 3 only. Do not display a company name.
- **Background:** Oil palm plantation photograph, with an overlay as needed to keep text and buttons readable.
- **Primary button:** Estate Dashboard — larger than the other buttons and centered horizontally. Dashboard contents are **TBD**, pending further requirements.
- **Secondary buttons:** Six smaller buttons in a two-column grid beneath the primary button.
- **Footer:** Display **v0.1 Alpha**, online/offline status, and the last successful offline-data synchronization time. Before the first successful synchronization, show **Not synced yet**. Any example synchronization date is a placeholder.
- **Offline intent:** Previously synchronized information should remain available without internet access. Refresh offline data through Settings; implementation details remain to be defined.

| Button | Intended contents |
| --- | --- |
| **Estate Dashboard** | Contents TBD |
| Estate Information | Estate information, including rainfall |
| Estate Production | Production information |
| Upkeep and Cultivation | Upkeep work, including weeding, manuring, pest and disease management |
| Reports | Estate reports |
| Settings | Application settings and offline data synchronization |
| About | Application information and version details |

## Layout reference

The diagram shows hierarchy and placement only. The intended background is an oil palm plantation photograph.

```text
+---------------------------------------+
|          Ladang Selendang 3            |
|                                       |
|   +-------------------------------+   |
|   |       Estate Dashboard        |   |
|   |         Contents TBD          |   |
|   +-------------------------------+   |
|                                       |
| +-----------------+ +---------------+ |
| | Estate          | | Estate        | |
| | Information     | | Production    | |
| +-----------------+ +---------------+ |
| +-----------------+ +---------------+ |
| | Upkeep and      | | Reports       | |
| | Cultivation     | |               | |
| +-----------------+ +---------------+ |
| +-----------------+ +---------------+ |
| | Settings        | | About         | |
| +-----------------+ +---------------+ |
|                                       |
| v0.1 Alpha  |  Online / Offline        |
| Last successful sync: Not synced yet  |
+---------------------------------------+
```

## Mockup availability

The latest generated mockup image from the planning conversation was not accessible when this reference was added. No replacement image is presented as the approved original. The requirements and schematic above preserve the approved layout until the original image can be supplied.

## Public repository, private estate data

Keep this repository public for collaboration. Publish design references and, if needed later, clearly fictional sample data only. Do not commit actual estate operational records, financial information, personal information, credentials, or private database exports.
