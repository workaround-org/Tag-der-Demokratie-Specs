# Sponsor Software – Spezifikation

## 1. Zweck

Die Sponsor-Software unterstützt das Team beim Erfassen, Verwalten und Nachverfolgen von Sponsoren für den Tag der Demokratie.

## 2. Kernanforderungen

1. Sponsoren anlegen, bearbeiten, archivieren  
2. Sponsoring-Level (z. B. Bronze/Silber/Gold/Platin) verwalten  
3. Vertrags- und Zahlungsstatus dokumentieren  
4. Kontaktpersonen pro Sponsor pflegen  
5. Übersicht über aktive Sponsoren und offene Zahlungen

## 3. Domänenmodell

### Entität: `Sponsor`

- `id`: Eindeutige ID (UUID)
- `name`: Firmen-/Organisationsname
- `level`: Sponsoring-Level
- `amountCommitted`: Zugesagter Betrag in EUR
- `amountPaid`: Bereits gezahlter Betrag in EUR
- `contractSigned`: Vertragsstatus
- `status`: Lebenszyklusstatus
- `contacts`: Liste von Kontaktpersonen
- `notes`: Interne Notizen
- `createdAt`, `updatedAt`: Zeitstempel

## 4. JSON Schema (Draft 2020-12)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://tag-der-demokratie.example/schemas/sponsor.schema.json",
  "title": "Sponsor",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "id",
    "name",
    "level",
    "amountCommitted",
    "amountPaid",
    "contractSigned",
    "status",
    "contacts",
    "createdAt",
    "updatedAt"
  ],
  "properties": {
    "id": {
      "type": "string",
      "format": "uuid"
    },
    "name": {
      "type": "string",
      "minLength": 2,
      "maxLength": 200
    },
    "level": {
      "type": "string",
      "enum": ["bronze", "silber", "gold", "platin", "partner"]
    },
    "amountCommitted": {
      "type": "number",
      "minimum": 0
    },
    "amountPaid": {
      "type": "number",
      "minimum": 0
    },
    "contractSigned": {
      "type": "boolean"
    },
    "status": {
      "type": "string",
      "enum": ["lead", "angefragt", "zugesagt", "aktiv", "abgeschlossen", "archiviert"]
    },
    "contacts": {
      "type": "array",
      "minItems": 1,
      "items": {
        "type": "object",
        "additionalProperties": false,
        "required": ["name", "email"],
        "properties": {
          "name": {
            "type": "string",
            "minLength": 2,
            "maxLength": 120
          },
          "email": {
            "type": "string",
            "format": "email"
          },
          "phone": {
            "type": "string",
            "minLength": 6,
            "maxLength": 40
          },
          "role": {
            "type": "string",
            "maxLength": 80
          }
        }
      }
    },
    "notes": {
      "type": "string",
      "maxLength": 4000
    },
    "createdAt": {
      "type": "string",
      "format": "date-time"
    },
    "updatedAt": {
      "type": "string",
      "format": "date-time"
    }
  }
}
```

## 5. Validierungsregeln

- `amountPaid` darf nicht größer als `amountCommitted` sein (Business-Regel, außerhalb des reinen JSON Schemas prüfbar)
- Bei `status = aktiv` muss `contractSigned = true` sein

