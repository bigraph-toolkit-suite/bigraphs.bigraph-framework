## [v0.7.0] - 2020-01-06
### Added
- refactored structure of the bigraph simulation-specific classes. Affects ReactiveSystem, added SimulationStrategy and BigraphModelChecker. ReactiveSystem is now regarded as a data container to specify and "program" the BRS #added #changed [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/0d4e9778d2143bd366b29e42a2e9c8b76b550743)
- compiler.xml added to gitignore [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/771fd98d64d8f095516b701c2643610570aa6ca3)
- new reactive system class "TransitionSystemBoundReactiveSystem" with reaction graph object #added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/bd00589386e3f6e50414a4ee1094b4489d344a6a)
- new reaction rule supplier which randomly selects the given rules #added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/a35b2b4da0f58c9ffb0474eb9dfdd0dd9cf05ba3)
- random simulation strategy implemented #added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/d9d46b5bd5b9579b7ef5255d82e6c17bc91d71ca)
- Predicates are exported now for BigraphER encoding #issue-10 #added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/d7644c02a52dc43f37b07723a918792728285636)
- multiple occurrences of a redex match in an agent are now implemented and added to the transition system [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/1586c38b5dbf2367e94734187db6e334a83c8567)
- source code reference added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/45cd0cbda419433b096935408e8c1c2630eae6e9)
- asynchroneously perform checking tasks, future is returned with reactionGraph #added [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/1cad8f3a0c123b2f2168f3aee948bdf1e3b48c77)
- apidocs path added to gitignore [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/d18c62aba987b6c4a884661278071f331e6b06e9)
### Changed
- renamed class RandomBigraphGenerator to class RandomBigraphGeneratorSupport #changed - more complexity measurement tests [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/1bd2b74c31a6c7cd4c5f79a9441a6ac27488fabc)
- changed output folder for generated test files [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/c00a86b0b1be0b2d4cb2d15e050a5515799f15be)
- refactored structure of the bigraph simulation-specific classes. Affects ReactiveSystem, added SimulationStrategy and BigraphModelChecker. ReactiveSystem is now regarded as a data container to specify and "program" the BRS #added #changed [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/0d4e9778d2143bd366b29e42a2e9c8b76b550743)
- renamed maven module bigraph-rewriting to bigraph-simulation to better reflect the intention of the module #changed [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/f674027e10349930763d70459c0f9d7ea9096849)
### Removed
- comments removed [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/ae2cdd3e3145c06961d1304e4fd70b42e299dcbe)
- mkdocs themes removed [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/6a2491e53df68d4a19e3db30bb2cfa81f2cfaeb1)
- mkdocs removed completely [view commit &#x2197;](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/commit/a384e288da25e66f05bf1f6f62bdd373f886d350)
### Bugfix

