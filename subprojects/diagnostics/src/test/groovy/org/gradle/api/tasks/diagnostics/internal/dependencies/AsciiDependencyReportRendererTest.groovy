/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.tasks.diagnostics.internal.dependencies

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.diagnostics.internal.graph.DependencyGraphRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.LegendRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.SimpleDependency
import org.gradle.internal.logging.text.TestStyledTextOutput
import org.gradle.test.fixtures.AbstractProjectBuilderSpec

class AsciiDependencyReportRendererTest extends AbstractProjectBuilderSpec {
    private final TestStyledTextOutput textOutput = new TestStyledTextOutput().ignoreStyle()
    private final AsciiDependencyReportRenderer renderer = new AsciiDependencyReportRenderer()

    def setup() {
        renderer.output = textOutput
        renderer.legendRenderer = Mock(LegendRenderer)
    }

    def "informs if no configurations"() {
        when:
        renderer.startProject(project);
        renderer.completeProject(project);

        then:
        textOutput.value.contains('No configurations')
    }

    def "shows configuration header"() {
        Configuration configuration1 = Mock()
        configuration1.getName() >> 'config1'
        configuration1.getDescription() >> 'description'
        configuration1.isCanBeResolved() >> true
        Configuration configuration2 = Mock()
        configuration2.getName() >> 'config2'
        configuration2.isCanBeResolved() >> true

        when:
        renderer.startConfiguration(configuration1);
        renderer.completeConfiguration(configuration1);
        renderer.startConfiguration(configuration2);
        renderer.completeConfiguration(configuration2);

        then:
        textOutput.value.readLines() == [
                'config1 - description',
                '',
                'config2'
        ]
    }

    def "renders dependency graph"() {
        renderer.dependencyGraphRenderer = Mock(DependencyGraphRenderer)
        def root = new SimpleDependency("root")
        root.children.add(new SimpleDependency("dep"))

        when:
        renderer.renderNow(root)

        then:
        1 * renderer.dependencyGraphRenderer.render(root)
    }

    def "renders legend on complete"() {
        renderer.dependencyGraphRenderer = Mock(DependencyGraphRenderer)

        when:
        renderer.complete()

        then:
        1 * renderer.legendRenderer.printLegend()
    }

    def "safely completes if no configurations"() {
        when:
        //no configuration started, and:
        renderer.complete()

        then:
        noExceptionThrown()
    }

    def "informs if no dependencies"() {
        def root = new SimpleDependency("root", true, "config")

        when:
        renderer.renderNow(root)

        then:
        textOutput.value.readLines() == ['No dependencies']
    }
}
