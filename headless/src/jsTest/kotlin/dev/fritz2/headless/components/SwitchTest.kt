package dev.fritz2.headless.components

import dev.fritz2.core.Id
import dev.fritz2.core.Keys
import dev.fritz2.core.RenderContext
import dev.fritz2.core.render
import dev.fritz2.headless.model.TestModel
import dev.fritz2.headless.test.getElementById
import dev.fritz2.headless.test.runTest
import dev.fritz2.headless.test.scopeTestKey
import dev.fritz2.headless.test.scopeTestValue
import dev.fritz2.validation.storeOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.KeyboardEventInit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SwitchTest {

    @Test
    fun testSwitch() = runTest {
        val name = "switch"
        val componentId = "$name-${Id.next()}"
        val componentState = storeOf(TestModel(), TestModel.validation).sub(TestModel.switch)

        render {
            switchWithLabel("classes", componentId, { set(scopeTestKey, scopeTestValue) }, RenderContext::button) {
                value(componentState)
                scope.asDataAttr(scopeTestKey)
                switchLabel {
                    +"Label"
                }
                switchDescription {
                    +"Description"
                }
                switchToggle {
                    attr("data-state", enabled.map { it.toString() })
                    attr("data-hasError", value.hasError.map { it.toString() })
                }
                switchValidationMessages(tag = RenderContext::ul) {
                    msgs.renderEach {
                        li { +it.message }
                    }
                }
            }
        }

        delay(100)
        val switchElement = getElementById<HTMLButtonElement>(componentId)
        assertEquals(componentId, switchElement.id)
        assertEquals("BUTTON", switchElement.tagName)
        assertEquals("classes", switchElement.className)
        assertEquals(scopeTestValue, switchElement.getAttribute("data-${scopeTestKey.name}"))

        val switchLabelElement = getElementById<HTMLLabelElement>("$componentId-label")
        assertEquals("Label", switchLabelElement.textContent, "wrong text-content")

        val switchDescriptionElement = getElementById<HTMLSpanElement>( "$componentId-description-0")
        assertEquals("Description", switchDescriptionElement.textContent, "wrong text-content")

        val switchToggleElement = getElementById<HTMLButtonElement>("$componentId-toggle")
        assertEquals("false", switchToggleElement.getAttribute("data-state"), "wrong state")
        assertEquals("false", switchToggleElement.getAttribute("data-hasError"), "wrong hasError")

        assertFails { getElementById<HTMLUListElement>("$componentId-validation-messages") }

        switchToggleElement.click()
        delay(100)
        assertEquals("true", switchToggleElement.getAttribute("data-state"), "wrong state after action click")
        assertEquals("true", switchToggleElement.getAttribute("data-hasError"), "wrong state after action click")
        val switchValidationMessages = getElementById<HTMLUListElement>("$componentId-validation-messages")
        assertEquals(1, switchValidationMessages.childElementCount, "wrong number of messages")

        val spacePress = KeyboardEvent("keydown", KeyboardEventInit(Keys.Space.key, Keys.Space.key))
        switchToggleElement.dispatchEvent(spacePress)
        delay(100)
        assertEquals("false", switchToggleElement.getAttribute("data-state"), "wrong state")
        assertEquals("false", switchToggleElement.getAttribute("data-hasError"), "wrong hasError")
        assertFails { getElementById<HTMLUListElement>("$componentId-validation-messages") }
    }

}