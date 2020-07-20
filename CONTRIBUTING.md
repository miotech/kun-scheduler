Contributing to KUN
=============================

KUN is an open source project and we love to receive contributions from our community — you! There are many ways to contribute, from writing tutorials or blog posts, improving the documentation, submitting bug reports and feature requests or writing code which can be incorporated into KUN itself.

Bug reports
-----------

If you think you have found a bug in KUN, first make sure that you are testing against the latest version of KUN - your issue may already have been fixed. If not, search our [issues list](https://github.com/miotech/KUN/issues) on GitHub in case a similar issue has already been opened.

It is very helpful if you can prepare a reproduction of the bug. In other words, provide a small test case which we can run to confirm your bug. It makes it easier to find the problem and to fix it. Provide as much information as you can. 

Feature requests
----------------

If you find yourself wishing for a feature that doesn't exist in KUN, you are probably not alone. There are bound to be others out there with similar needs. Many of the features that KUN has today have been added because our users saw the need.
Open an issue on our [issues list](https://github.com/miotech/KUN/issues) on GitHub which describes the feature you would like to see, why you need it, and how it should work.

Contributing code and documentation changes
-------------------------------------------

If you would like to contribute a new feature or a bug fix to KUN,
please discuss your idea first on the Github issue. If there is no Github issue
for your idea, please open one. It may be that somebody is already working on
it, or that there are particular complexities that you should know about before
starting the implementation. There are often a number of ways to fix a problem
and it is important to find the right approach before spending time on a PR
that cannot be merged.

We add the `help wanted` label to existing Github issues for which community
contributions are particularly welcome, and we use the `good first issue` label
to mark issues that we think will be suitable for new contributors.

### Submitting your changes

Once your changes and tests are ready to submit for review:

1. Test your changes

    Run the test suite to make sure that nothing is broken. 

2. Rebase your changes

    Update your local repository with the most recent code from the main Elasticsearch repository, and rebase your branch on top of the latest master branch. We prefer your initial changes to be squashed into a single commit. Later, if we ask you to make changes, add them as separate commits.  This makes them easier to review.  As a final step before merging we will either ask you to squash all commits yourself or we'll do it for you.


3. Submit a pull request

    Push your local changes to your forked copy of the repository and [submit a pull request](https://help.github.com/articles/using-pull-requests). In the pull request, choose a title which sums up the changes that you have made, and in the body provide more details about what your changes do. Also mention the number of the issue where discussion has taken place, eg "Closes #123".

Then sit back and wait. There will probably be discussion about the pull request and, if any changes are needed, we would love to work with you to get your pull request merged into KUN.

Please adhere to the general guideline that you should never force push
to a publicly shared branch. Once you have opened your pull request, you
should consider your branch publicly shared. Instead of force pushing
you can just add incremental commits; this is generally easier on your
reviewers. If you need to pick up changes from master, you can merge
master into your branch. A reviewer might ask you to rebase a
long-running pull request in which case force pushing is okay for that
request. Note that squashing at the end of the review process should
also not be done, that can be done when the pull request is [integrated
via GitHub](https://github.com/blog/2141-squash-your-commits).

### Java Language Formatting Guidelines

* Java indent is 4 spaces
* Line width is 140 characters
* Lines of code surrounded by `// tag::NAME` and `// end::NAME` comments are included
  in the documentation and should only be 76 characters wide not counting
  leading indentation. Such regions of code are not formatted automatically as
  it is not possible to change the line length rule of the formatter for
  part of a file. Please format such sections sympathetically with the rest
  of the code, while keeping lines to maximum length of 76 characters.
* Wildcard imports (`import foo.bar.baz.*`) are forbidden and will cause
  the build to fail.
* If *absolutely* necessary, you can disable formatting for regions of code
  with the `// tag::NAME` and `// end::NAME` directives, but note that
  these are intended for use in documentation, so please make it clear what
  you have done, and only do this where the benefit clearly outweighs the
  decrease in consistency.
* Note that Javadoc and block comments i.e. `/* ... */` are not formatted,
  but line comments i.e `// ...` are.
* There is an implicit rule that negative boolean expressions should use
  the form `foo == false` instead of `!foo` for better readability of the
  code. While this isn't strictly enforced, if might get called out in PR
  reviews as something to change.

### Javadoc

Good Javadoc can help with navigating and understanding code. KUN 
has some guidelines around when to write Javadoc and when not to, but note
that we don't want to be overly prescriptive. The intent of these guidelines
is to be helpful, not to turn writing code into a chore.

#### The short version

   1. Always add Javadoc to new code.
   2. Add Javadoc to existing code if you can.
   3. Document the "why", not the "how", unless that's important to the
      "why".
   4. Don't document anything trivial or obvious (e.g. getters and
      setters). In other words, the Javadoc should add some value.

#### The long version

   1. If you add a new Java package, please also add package-level
      Javadoc that explains what the package is for. This can just be a
      reference to a more foundational / parent package if appropriate. An
      example would be a package hierarchy for a new feature or plugin -
      the package docs could explain the purpose of the feature, any
      caveats, and possibly some examples of configuration and usage.
   2. New classes and interfaces must have class-level Javadoc that
      describes their purpose. There are a lot of classes in the
      Elasticsearch repository, and it's easier to navigate when you
      can quickly find out what is the purpose of a class. This doesn't
      apply to inner classes or interfaces, unless you expect them to be
      explicitly used outside their parent class.
   3. New public methods must have Javadoc, because they form part of the
      contract between the class and its consumers. Similarly, new abstract
      methods must have Javadoc because they are part of the contract
      between a class and its subclasses. It's important that contributors
      know why they need to implement a method, and the Javadoc should make
      this clear. You don't need to document a method if it's overriding an
      abstract method (either from an abstract superclass or an interface),
      unless your implementation is doing something "unexpected" e.g. deviating
      from the intent of the original method.
   4. Following on from the above point, please add docs to existing public
      methods if you are editing them, or to abstract methods if you can.
   5. Non-public, non-abstract methods don't require Javadoc, but if you feel
      that adding some would make it easier for other developers to
      understand the code, or why it's written in a particular way, then please
      do so.
   6. Properties don't need to have Javadoc, but please add some if there's
      something useful to say.
   7. Javadoc should not go into low-level implementation details unless
      this is critical to understanding the code e.g. documenting the
      subtleties of the implementation of a private method. The point here
      is that implementations will change over time, and the Javadoc is
      less likely to become out-of-date if it only talks about the what is
      the purpose of the code, not what it does.
   8. Examples in Javadoc can be very useful, so feel free to add some if
      you can reasonably do so i.e. if it takes a whole page of code to set
      up an example, then Javadoc probably isn't the right place for it.
      Longer or more elaborate examples are probably better suited
      to the package docs.
   9. Test methods are a good place to add Javadoc, because you can use it
      to succinctly describe e.g. preconditions, actions and expectations
      of the test, more easily that just using the test name alone. Please
      consider documenting your tests in this way.
   10. Sometimes you shouldn't add Javadoc:
       1. Where it adds no value, for example where a method's
          implementation is trivial such as with getters and setters, or a
          method just delegates to another object.
       2. However, you should still add Javadoc if there are caveats around
          calling a method that are not immediately obvious from reading the
          method's implementation in isolation.
       3. You can omit Javadoc for simple classes, e.g. where they are a
          simple container for some data. However, please consider whether a
          reader might still benefit from some additional background, for
          example about why the class exists at all.
   11. Not all comments need to be Javadoc. Sometimes it will make more
       sense to add comments in a method's body, for example due to important
       implementation decisions or "gotchas". As a general guide, if some
       information forms part of the contract between a method and its callers,
       then it should go in the Javadoc, otherwise you might consider using
       regular comments in the code. Remember as well that Elasticsearch
       has extensive [user documentation](./docs), and it is not the role
       of Javadoc to replace that.
   12. Please still try to make class, method or variable names as
       descriptive and concise as possible, as opposed to relying solely on
       Javadoc to describe something.
   13. Use `@link` and `@see` to add references, either to related
       resources in the codebase or to relevant external resources.
   14. If you need help writing Javadoc, just ask!

Finally, use your judgement! Base your decisions on what will help other
developers - including yourself, when you come back to some code
3 months in the future, having forgotten how it works.

### Project layout

#### `TODO`

### Gradle Build

We use Gradle to build KUN because it is flexible enough to not only
build and package KUN, but also orchestrate all of the ways that we
have to test KUN.

#### Configurations

Gradle organizes dependencies and build artifacts into "configurations" and
allows you to use these configurations arbitrarily. Here are some of the most
common configurations in our build and how we use them:

<dl>
<dt>`compile`</dt><dd>Code that is on the classpath at both compile and
runtime.</dd>
<dt>`runtime`</dt><dd>Code that is not on the classpath at compile time but is
on the classpath at runtime. We mostly use this configuration to make sure that
we do not accidentally compile against dependencies of our dependencies also
known as "transitive" dependencies".</dd>
<dt>`compileOnly`</dt><dd>Code that is on the classpath at compile time but that
should not be shipped with the project because it is "provided" by the runtime
somehow. Elasticsearch plugins use this configuration to include dependencies
that are bundled with Elasticsearch's server.</dd>
<dt>`testImplementation`</dt><dd>Code that is on the classpath for compiling tests
that are part of this project but not production code. The canonical example
of this is `junit`.</dd>
</dl>


Reviewing and accepting your contribution
-----------------------------------------

We review every contribution carefully to ensure that the change is of high
quality and fits well with the rest of the codebase. If accepted,
we will merge your change and usually take care of backporting it to
appropriate branches ourselves.

We really appreciate everyone who is interested in contributing to
KUN and regret that we sometimes have to reject contributions even
when they might appear to make genuine improvements to the system. Reviewing
contributions can be a very time-consuming task, yet the team is small and our
time is very limited. In some cases the time we would need to spend on reviews
would outweigh the benefits of a change by preventing us from working on other
more beneficial changes instead.

Please discuss your change in a Github issue before spending much time on its
implementation. We sometimes have to reject contributions that duplicate other
efforts, take the wrong approach to solving a problem, or solve a problem which
does not need solving. An up-front discussion often saves a good deal of wasted
time in these cases.
