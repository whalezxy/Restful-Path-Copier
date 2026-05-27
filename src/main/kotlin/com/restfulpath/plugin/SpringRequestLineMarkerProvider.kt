package com.restfulpath.plugin

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import com.intellij.util.Function
import java.awt.datatransfer.StringSelection
import com.intellij.psi.PsiModifierListOwner

class SpringRequestLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val identifier = element as? PsiIdentifier ?: return null
        val method = identifier.parent as? PsiMethod ?: return null
        if (method.nameIdentifier != identifier) return null
        if (!isSpringMapping(method)) return null

        val icon = AllIcons.Actions.Copy
        val tooltip = "Copy full request path"
        val handler = GutterIconNavigationHandler<PsiElement> { _, elt ->
            val psiMethod = (elt as? PsiIdentifier)?.parent as? PsiMethod ?: return@GutterIconNavigationHandler
            val fullPath = buildFullPath(psiMethod) ?: return@GutterIconNavigationHandler
            CopyPasteManager.getInstance().setContents(StringSelection(fullPath))
            Notifications.Bus.notify(
                Notification(
                    "RestfulPathCopier",
                    "Request Path Copied",
                    fullPath,
                    NotificationType.INFORMATION
                ),
                psiMethod.project
            )
        }

        return LineMarkerInfo(
            identifier,
            identifier.textRange,
            icon,
            Function { tooltip },
            handler,
            GutterIconRenderer.Alignment.LEFT
        )
    }

    private fun isSpringMapping(method: PsiMethod): Boolean {
        return getMappingPath(method) != null
    }

    private fun buildFullPath(method: PsiMethod): String? {
        val classPath = method.containingClass?.let { getMappingPath(it) }.orEmpty()
        val methodPath = getMappingPath(method) ?: return null
        return joinPaths(classPath, methodPath)
    }

    private fun getMappingPath(owner: PsiModifierListOwner): String? {
        val annotation = owner.modifierList?.annotations?.firstOrNull { it.isSpringMappingAnnotation() } ?: return null
        return extractPath(annotation)
    }

    private fun PsiAnnotation.isSpringMappingAnnotation(): Boolean {
        val name = qualifiedName ?: return false
        return name == "org.springframework.web.bind.annotation.RequestMapping" ||
            name == "org.springframework.web.bind.annotation.GetMapping" ||
            name == "org.springframework.web.bind.annotation.PostMapping" ||
            name == "org.springframework.web.bind.annotation.PutMapping" ||
            name == "org.springframework.web.bind.annotation.DeleteMapping" ||
            name == "org.springframework.web.bind.annotation.PatchMapping"
    }

    private fun extractPath(annotation: PsiAnnotation): String? {
        val attrs = annotation.parameterList.attributes
        val valueAttr = attrs.firstOrNull { it.name == null || it.name == "value" || it.name == "path" } ?: return null
        val value = valueAttr.value ?: return null
        val text = value.text.trim()
        if (text.startsWith("{")) {
            val first = text.removePrefix("{").removeSuffix("}").split(",").firstOrNull()?.trim() ?: return null
            return stripQuotes(first)
        }
        return stripQuotes(text)
    }

    private fun stripQuotes(text: String): String {
        var result = text.trim()
        if (result.startsWith('"') && result.endsWith('"') && result.length >= 2) {
            result = result.substring(1, result.length - 1)
        }
        return normalizePath(result)
    }

    private fun normalizePath(path: String): String {
        if (path.isBlank()) return ""
        val trimmed = path.trim()
        return if (trimmed.startsWith("/")) trimmed else "/$trimmed"
    }

    private fun joinPaths(base: String, child: String): String {
        val a = base.trim().trimEnd('/')
        val b = child.trim().trimStart('/')
        return when {
            a.isBlank() -> normalizePath(b)
            b.isBlank() -> normalizePath(a)
            else -> "$a/$b"
        }
    }
}
