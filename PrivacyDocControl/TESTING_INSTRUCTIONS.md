# Manual Testing Instructions for Privacy Document Control System

## 1. Document Upload
- Go to the document upload page (e.g., `/upload`).
- Upload a supported document file (PDF, DOCX, TXT, JPG, PNG, etc.).
- After upload, note the generated token displayed on the success page.

## 2. Token Access and Viewing
- Go to the staff token input page (e.g., `/staff/view`).
- Enter the token exactly as provided (case-insensitive).
- Verify the document details and preview are shown.
- Confirm the remaining time before expiration is displayed (should be ~15 minutes).

## 3. Print Document
- Use the print button or form on the document view page.
- Confirm the document is marked as printed and the file is deleted.
- Attempt to access the document again with the same token; it should show an error.

## 4. Expiration Behavior
- Upload a document and note the token.
- Wait for 15 minutes (or adjust system time for testing).
- Attempt to access the document with the token.
- Confirm the document is no longer accessible and an expiration error is shown.
- Confirm the file is deleted from the server.

## 5. Download Prevention
- When viewing the document, verify it is served inline (e.g., PDF viewer in browser).
- Confirm there is no option to download the document.
- Try to right-click or use browser controls to download; it should be disabled or prevented.

## 6. Scheduled Cleanup
- Upload multiple documents.
- Wait for expiration time.
- Confirm expired documents are automatically deleted by the scheduled cleanup task.

---

If you want, I can also help you write automated test cases for these scenarios.
